package com.plusmobileapps.paging

import com.plusmobileapps.konnectivity.Konnectivity
import com.plusmobileapps.paging.PagingDataSource.CacheInfo
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

object PagingDataSourceFactory : PagingDataSource.Factory {
    private val konnectivity = Konnectivity()
    private val settings = Settings()
    override fun <INPUT, DATA> create(
        cacheInfo: CacheInfo?,
        pageLoader: PageLoader<INPUT, DATA>
    ): PagingDataSource<INPUT, DATA> {
        return PagingDataSourceImpl(
            cacheInfo = cacheInfo,
            ioContext = Dispatchers.Default,
            pageLoader = pageLoader,
            konnectivity = konnectivity,
            settings = settings,
            getCurrentInstant = { Clock.System.now() }
        )
    }
}

internal class PagingDataSourceImpl<INPUT, DATA>(
    private val cacheInfo: CacheInfo?,
    ioContext: CoroutineDispatcher,
    private val pageLoader: PageLoader<INPUT, DATA>,
    private val konnectivity: Konnectivity,
    private val settings: Settings,
    private val getCurrentInstant: () -> Instant,
) : PagingDataSource<INPUT, DATA> {

    private val scope = CoroutineScope(ioContext)

    private val firstPageCacheKey = "${cacheInfo?.cachingKey}-first-page-caching-key"
    private val nextPagePagingKey = "${cacheInfo?.cachingKey}-next-page-paging-key"

    private val pagingState = MutableStateFlow<State<INPUT, DATA>>(
        State(
            pagingKey = settings.getStringOrNull(nextPagePagingKey)?.let {
                it.takeIf { isFirstPageCacheValid() }
            },
        )
    )
    private val pagingKey: String?
        get() = pagingState.value.pagingKey

    override val state: StateFlow<PagingDataSource.State<DATA>> =
        pagingState.asStateFlow()
            .map(scope) {
                PagingDataSource.State(
                    isFirstPageLoading = it.firstPageIsLoading,
                    isNextPageLoading = it.nextPageIsLoading,
                    data = it.data,
                    pageLoaderError = (it.pageLoaderState as? PageLoaderState.Failed)?.exception,
                    hasMoreToLoad = (it.pageLoaderState as? PageLoaderState.Idle)?.hasMorePages
                        ?: false
                )
            }

    override fun clearAndLoadFirstPage(input: INPUT) {
        if (isFirstPageCacheValid()) {
            pagingState.value = pagingState.value.copy(input = input)
            return
        }
        if (pagingState.value.firstPageIsLoading) return
        pagingState.value = State(
            input = input,
            pageLoaderState = PageLoaderState.Loading(
                isFirstPage = true
            ),
        )
        scope.launch { sendRequest(input, true) }
    }

    override fun loadNextPage() {
        val currentState = pagingState.value
        if (currentState.pageLoaderState is PageLoaderState.Loading) return
        pagingState.value = pagingState.value.copy(
            pageLoaderState = PageLoaderState.Loading(
                isFirstPage = pagingKey == null
            ),
        )
        scope.launch {
            sendRequest(
                input = currentState.input,
                isFirstPage = (currentState.pageLoaderState as? PageLoaderState.Failed)?.exception?.isFirstPage == true
            )
        }
    }

    private suspend fun sendRequest(input: INPUT?, isFirstPage: Boolean) {
        if (!konnectivity.isConnected) {
            pagingState.value = pagingState.value.copy(
                pageLoaderState = PageLoaderState.Failed(
                    exception = PageLoaderException.NoNetworkException(isFirstPage),
                )
            )
            return
        }
        val response: PageLoaderResponse<DATA> = pageLoader.load(
            PageLoaderRequest(
                pagingKey = pagingKey,
                input = input
                    ?: throw IllegalStateException("Attempting to load next page without loading first page since the input is null"),
            )
        )
        val currentState = pagingState.value
        when (response) {
            is PageLoaderResponse.Error -> {
                pagingState.value = pagingState.value.copy(
                    pageLoaderState = PageLoaderState.Failed(
                        PageLoaderException.GeneralError(
                            exception = response.exception,
                            errorMessage = response.message,
                            isFirstPage = isFirstPage
                        )
                    ),
                )
            }
            is PageLoaderResponse.Success -> {
                val (data, pagingToken) = response
                pagingState.value = pagingState.value.copy(
                    pageLoaderState = PageLoaderState.Idle(
                        hasMorePages = pagingToken != null
                    ),
                    data = currentState.data + data,
                    pagingKey = response.pagingToken
                )
                if (cacheInfo != null && isFirstPage) {
                    settings.putString(firstPageCacheKey, getCurrentInstant().toString())
                }
                if (pagingToken != null && cacheInfo != null && !isFirstPage) {
                    settings.putString(nextPagePagingKey, pagingToken)
                }
            }
        }
    }

    private fun <T, M> StateFlow<T>.map(
        coroutineScope: CoroutineScope,
        mapper: (value: T) -> M
    ): StateFlow<M> = map { mapper(it) }.stateIn(
        coroutineScope,
        SharingStarted.Eagerly,
        mapper(value)
    )

    private fun isFirstPageCacheValid(): Boolean {
        if (cacheInfo == null) return false
        val lastFetchedFirstPageKeyInstant = settings.getStringOrNull(firstPageCacheKey)?.let {
            Instant.parse(it)
        } ?: return false
        val now = getCurrentInstant()
        val durationSinceLastFetch: Duration = now - lastFetchedFirstPageKeyInstant
        return durationSinceLastFetch < cacheInfo.ttl
    }

    private data class State<INPUT, DATA>(
        val input: INPUT? = null,
        val pageLoaderState: PageLoaderState = PageLoaderState.Idle(hasMorePages = true),
        val data: List<DATA> = emptyList(),
        val pagingKey: String? = null,
    ) {
        val firstPageIsLoading: Boolean
            get() = (pageLoaderState as? PageLoaderState.Loading)?.isFirstPage ?: false

        val nextPageIsLoading: Boolean
            get() = (pageLoaderState as? PageLoaderState.Loading)?.let {
                !it.isFirstPage
            } ?: false
    }
}