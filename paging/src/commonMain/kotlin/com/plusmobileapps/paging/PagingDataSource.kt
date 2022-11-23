package com.plusmobileapps.paging

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*

interface PagingDataSource<INPUT, DATA> {
    val pageLoaderData: StateFlow<PageLoaderData<DATA>>
    fun loadFirstPage(
        input: INPUT,
        pageSize: Int,
        requestKey: String,
    )

    fun loadNextPage(
        input: INPUT,
        requestKey: String,
    )

    interface Factory {
        fun <INPUT, DATA> create(pageLoader: PageLoader<INPUT, DATA>): PagingDataSource<INPUT, DATA>
    }
}

object PagingDataSourceFactory : PagingDataSource.Factory {
    override fun <INPUT, DATA> create(pageLoader: PageLoader<INPUT, DATA>): PagingDataSource<INPUT, DATA> {
        return PagingDataSourceImpl<INPUT, DATA>(
            ioContext = Dispatchers.Default,
            pageLoader = pageLoader,
        )
    }
}

internal class PagingDataSourceImpl<INPUT, DATA>(
    ioContext: CoroutineDispatcher,
    private val pageLoader: PageLoader<INPUT, DATA>,
) : PagingDataSource<INPUT, DATA> {

    private val scope = CoroutineScope(ioContext)

    private val pagingState = MutableStateFlow<State<DATA>>(State())
    private val pageSize: Int
        get() = pagingState.value.pageSize
    private val pagingKey: String?
        get() = pagingState.value.pagingKey

    override val pageLoaderData: StateFlow<PageLoaderData<DATA>> =
        pagingState.asStateFlow().map(scope) { PageLoaderData(it.pageLoaderState, it.data) }

    override fun loadFirstPage(input: INPUT, pageSize: Int, requestKey: String) {
        pagingState.value = pagingState.value.copy(pageSize = pageSize, pagingKey = null)
        scope.launch { sendRequest(input) }
    }

    override fun loadNextPage(
        input: INPUT,
        requestKey: String,
    ) {
        val currentState = pagingState.value
        if (currentState.pageLoaderState is PageLoaderState.Loading) return
        scope.launch { sendRequest(input) }
    }

    private suspend fun sendRequest(input: INPUT) {
        pagingState.value = pagingState.value.copy(
            pageLoaderState = PageLoaderState.Loading(
                isFirstPage = pagingKey == null
            )
        )
        val response: PageLoaderResponse<DATA> = pageLoader(
            PageLoaderRequest(
                pagingKey = pagingKey,
                pageSize = pageSize,
                input = input,
            )
        )
        val currentState = pagingState.value
        when (response) {
            is PageLoaderResponse.Error -> {
                val (canRetrySameRequest, message) = response
                pagingState.value = pagingState.value.copy(
                    pageLoaderState = PageLoaderState.Failed(canRetrySameRequest, message),
                    data = currentState.data,
                )
            }
            is PageLoaderResponse.Success -> {
                val (data, pagingToken) = response
                pagingState.value = State(
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
        coroutineScope : CoroutineScope,
        mapper : (value : T) -> M
    ) : StateFlow<M> = map { mapper(it) }.stateIn(
        coroutineScope,
        SharingStarted.Eagerly,
        mapper(value)
    )

    private data class State<DATA>(
        val pageLoaderState: PageLoaderState = PageLoaderState.Idle(hasMorePages = true),
        val data: List<DATA> = emptyList(),
        val pageSize: Int = 0,
        val pagingKey: String? = null
    )
}