package com.plusmobileapps.paging

import com.plusmobileapps.konnectivity.Konnectivity
import com.plusmobileapps.paging.CachedPageLoader.CacheInfo
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlin.time.Duration

internal class CachedPageLoaderImpl<INPUT, DATA>(
    private val cacheInfo: CacheInfo?,
    ioContext: CoroutineDispatcher,
    private val reader: () -> Flow<List<DATA>>,
    private val writer: suspend (List<DATA>) -> Unit,
    private val deleteAllAndWrite: suspend (List<DATA>) -> Unit,
    private val pageLoader: PageLoader<INPUT, DATA>,
    private val konnectivity: Konnectivity,
    private val settings: Settings,
    private val getCurrentInstant: () -> Instant,
) : CachedPageLoader<INPUT, DATA> {

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

    init {
        collectFromReader()
    }

    override val state: StateFlow<PagingDataSourceState<DATA>> =
        pagingState.asStateFlow()
            .map(scope) { it.toPagingDataSourceState() }

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

    private fun collectFromReader() {
        scope.launch {
            reader().collect { results ->
                val currentState = pagingState.value
                pagingState.value = currentState.copy(
                    data = results,
                    pageLoaderState = if (results.isNotEmpty() && currentState.pageLoaderState is PageLoaderState.Failed) {
                        PageLoaderState.Failed(
                            PageLoaderException.FirstPageErrorWithCachedResults(
                                currentState.pageLoaderState.exception
                            )
                        )
                    } else {
                        currentState.pageLoaderState
                    }
                )
            }
        }
    }
}