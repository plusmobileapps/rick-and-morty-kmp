package com.plusmobileapps.paging

import com.plusmobileapps.konnectivity.Konnectivity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class InMemoryPageLoaderImpl<INPUT, DATA>(
    ioContext: CoroutineDispatcher,
    private val pageLoader: PageLoader<INPUT, DATA>,
    private val konnectivity: Konnectivity,
) : InMemoryPageLoader<INPUT, DATA> {

    private val scope = CoroutineScope(ioContext)

    private val pagingState = MutableStateFlow<State<INPUT, DATA>>(State())
    private val pagingKey: String?
        get() = pagingState.value.pagingKey

    override val state: StateFlow<PagingDataSourceState<DATA>> =
        pagingState.asStateFlow()
            .map(scope) { it.toPagingDataSourceState() }

    override fun clearAndLoadFirstPage(input: INPUT) {
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
                    pagingKey = response.pagingToken,
                )
            }
        }
    }
}