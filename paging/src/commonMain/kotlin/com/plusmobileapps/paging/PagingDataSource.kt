package com.plusmobileapps.paging

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*

interface PagingDataSource<INPUT, DATA> {

    val pageLoaderData: StateFlow<PageLoaderData<DATA>>

    fun clearAndLoadFirstPage(input: INPUT, pageSize: Int)

    fun loadNextPage()

    interface Factory {
        fun <INPUT, DATA> create(pageLoader: PageLoader<INPUT, DATA>): PagingDataSource<INPUT, DATA>
    }
}

object PagingDataSourceFactory : PagingDataSource.Factory {
    override fun <INPUT, DATA> create(
        pageLoader: PageLoader<INPUT, DATA>
    ): PagingDataSource<INPUT, DATA> = PagingDataSourceImpl(
        ioContext = Dispatchers.Default,
        pageLoader = pageLoader,
    )
}

internal class PagingDataSourceImpl<INPUT, DATA>(
    ioContext: CoroutineDispatcher,
    private val pageLoader: PageLoader<INPUT, DATA>,
) : PagingDataSource<INPUT, DATA> {

    private val scope = CoroutineScope(ioContext)

    private val pagingState = MutableStateFlow<State<INPUT, DATA>>(State())
    private val pageSize: Int
        get() = pagingState.value.pageSize
    private val pagingKey: String?
        get() = pagingState.value.pagingKey

    override val pageLoaderData: StateFlow<PageLoaderData<DATA>> =
        pagingState.asStateFlow()
            .map(scope) {
                PageLoaderData(
                    isFirstPageLoading = it.firstPageIsLoading,
                    isNextPageLoading = it.nextPageIsLoading,
                    data = it.data,
                    pageLoaderError = (it.pageLoaderState as? PageLoaderState.Failed)?.let { error ->
                        if (it.pageLoaderState.isFirstPage) {
                            PageLoaderError.FirstPage(error.message)
                        } else {
                            PageLoaderError.NextPage(error.message)
                        }
                    },
                    hasMoreToLoad = (it.pageLoaderState as? PageLoaderState.Idle)?.hasMorePages ?: false
                )
            }

    override fun clearAndLoadFirstPage(input: INPUT, pageSize: Int) {
        pagingState.value = State(
            input = input,
            pageSize = pageSize,
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
                isFirstPage = (currentState.pageLoaderState as? PageLoaderState.Failed)?.isFirstPage == true
            )
        }
    }

    private suspend fun sendRequest(input: INPUT?, isFirstPage: Boolean) {
        val response: PageLoaderResponse<DATA> = pageLoader(
            PageLoaderRequest(
                pagingKey = pagingKey,
                pageSize = pageSize,
                input = input
                    ?: throw IllegalStateException("Attempting to load next page without loading first page since the input is null"),
            )
        )
        val currentState = pagingState.value
        when (response) {
            is PageLoaderResponse.Error -> {
                val (canRetrySameRequest, message) = response
                pagingState.value = pagingState.value.copy(
                    pageLoaderState = PageLoaderState.Failed(
                        canRetrySameRequest = canRetrySameRequest,
                        message = message,
                        isFirstPage = isFirstPage
                    ),
                    data = currentState.data,
                )
            }
            is PageLoaderResponse.Success -> {
                val (data, pagingToken) = response
                pagingState.value = pagingState.value.copy(
                    pageLoaderState = PageLoaderState.Idle(
                        hasMorePages = pagingToken != null
                    ),
                    data = currentState.data + data,
                    pageSize = pageSize,
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
        val pageSize: Int = 0,
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