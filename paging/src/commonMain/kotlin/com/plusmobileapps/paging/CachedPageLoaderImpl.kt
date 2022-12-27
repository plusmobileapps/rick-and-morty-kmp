package com.plusmobileapps.paging

import com.plusmobileapps.konnectivity.Konnectivity
import com.plusmobileapps.paging.CachedPageLoader.CacheInfo
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlin.time.Duration

internal class CachedPageLoaderImpl<INPUT, DATA>(
    private val cacheInfo: CacheInfo?,
    private val ioContext: CoroutineDispatcher,
    reader: () -> Flow<List<DATA>>,
    private val writer: suspend (List<DATA>) -> Unit,
    private val deleteAllAndWrite: suspend (List<DATA>) -> Unit,
    private val pageLoader: PageLoader<INPUT, DATA>,
    private val konnectivity: Konnectivity,
    private val settings: Settings,
    private val getCurrentInstant: () -> Instant,
) : CachedPageLoader<INPUT, DATA> {

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

    override val state: Flow<PagingDataSourceState<DATA>> = combine(
        flow = pagingState.asStateFlow(),
        flow2 = reader(),
        transform = ::mapPagingStateAndReader
    ).distinctUntilChanged()

    override suspend fun loadFirstPage(input: INPUT) = withContext(ioContext) {
        if (isFirstPageCacheValid()) {
            pagingState.value = pagingState.value.copy(input = input)
            return@withContext
        }
        if (pagingState.value.firstPageIsLoading) return@withContext
        pagingState.value = State(
            input = input,
            pageLoaderState = PageLoaderState.Loading(
                isFirstPage = true
            ),
        )
        sendRequest(input, true)
    }

    override suspend fun loadNextPage() = withContext(ioContext) {
        val currentState = pagingState.value
        if (currentState.pageLoaderState is PageLoaderState.Loading) return@withContext
        pagingState.value = pagingState.value.copy(
            pageLoaderState = PageLoaderState.Loading(
                isFirstPage = pagingKey == null
            ),
        )
        sendRequest(
            input = currentState.input,
            isFirstPage = (currentState.pageLoaderState as? PageLoaderState.Failed)?.exception?.isFirstPage == true
        )
    }

    private suspend fun sendRequest(input: INPUT?, isFirstPage: Boolean) {
        if (!konnectivity.isConnected) {
            pagingState.value = pagingState.value.copy(
                pageLoaderState = if (pagingState.value.data.isEmpty() || !isFirstPage) {
                    PageLoaderState.Failed(
                        exception = PageLoaderException.NoNetworkException(isFirstPage),
                    )
                } else {
                    PageLoaderState.Failed(
                        exception = PageLoaderException.FirstPageErrorWithCachedResults(
                            PageLoaderException.NoNetworkException(true)
                        )
                    )
                }
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
                if (isFirstPage) {
                    deleteAllAndWrite(data)
                } else {
                    writer(data)
                }
                pagingState.value = pagingState.value.copy(
                    pageLoaderState = PageLoaderState.Idle(
                        hasMorePages = pagingToken != null
                    ),
                    pagingKey = response.pagingToken,
                )
                if (isFirstPage) {
                    settings.putString(firstPageCacheKey, getCurrentInstant().toString())
                }
                if (pagingToken != null) {
                    settings.putString(nextPagePagingKey, pagingToken)
                }
            }
        }
    }

    private fun isFirstPageCacheValid(): Boolean {
        if (cacheInfo == null) return false
        val lastFetchedFirstPageKeyInstant = settings.getStringOrNull(firstPageCacheKey)?.let {
            Instant.parse(it)
        } ?: return false
        val now = getCurrentInstant()
        val durationSinceLastFetch: Duration = now - lastFetchedFirstPageKeyInstant
        return durationSinceLastFetch < cacheInfo.ttl
    }

    private fun mapPagingStateAndReader(
        pagingState: State<INPUT, DATA>,
        data: List<DATA>
    ): PagingDataSourceState<DATA> = PagingDataSourceState(
        isFirstPageLoading = pagingState.firstPageIsLoading,
        isNextPageLoading = pagingState.nextPageIsLoading,
        data = data,
        pageLoaderError = if (data.isNotEmpty() && pagingState.pageLoaderState is PageLoaderState.Failed) {
            PageLoaderException.FirstPageErrorWithCachedResults(
                pagingState.pageLoaderState.exception
            )
        } else {
            (pagingState.pageLoaderState as? PageLoaderState.Failed)?.exception
        },
        hasMoreToLoad = pagingState.hasMoreToLoad
    )

}