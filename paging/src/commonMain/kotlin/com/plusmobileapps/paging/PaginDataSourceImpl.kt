package com.plusmobileapps.paging

import com.plusmobileapps.konnectivity.Konnectivity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

object PagingDataSourceFactory : PagingDataSource.Factory {
    private val konnectivity = Konnectivity()
    override fun <INPUT, DATA> create(
        pageLoader: PageLoader<INPUT, DATA>
    ): PagingDataSource<INPUT, DATA> = PagingDataSourceImpl(
        ioContext = Dispatchers.Default,
        pageLoader = pageLoader,
        konnectivity = konnectivity,
    )
}

internal class PagingDataSourceImpl<INPUT, DATA>(
    ioContext: CoroutineDispatcher,
    private val pageLoader: PageLoader<INPUT, DATA>,
    private val konnectivity: Konnectivity,
) : PagingDataSource<INPUT, DATA> {

    private val scope = CoroutineScope(ioContext)

    private val pagingState = MutableStateFlow<State<INPUT, DATA>>(State())
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
        val response: PageLoaderResponse<DATA> = pageLoader(
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
            }
        }
    }

    fun <T, M> StateFlow<T>.map(
        coroutineScope: CoroutineScope,
        mapper: (value: T) -> M
    ): StateFlow<M> = map { mapper(it) }.stateIn(
        coroutineScope,
        SharingStarted.Eagerly,
        mapper(value)
    )

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