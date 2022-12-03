package com.plusmobileapps.paging

import com.plusmobileapps.konnectivity.Konnectivity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*

/**
 * A data source that can load pages and manage the state of loading pages.
 *
 * @param INPUT The type of input to be used on a [PageLoaderRequest].
 * @param DATA The type of data to be return from [PageLoaderResponse].
 */
interface PagingDataSource<INPUT, DATA> {

    val state: StateFlow<State<DATA>>

    fun clearAndLoadFirstPage(input: INPUT)

    fun loadNextPage()

    interface Factory {
        fun <INPUT, DATA> create(
            pageLoader: PageLoader<INPUT, DATA>,
        ): PagingDataSource<INPUT, DATA>
    }

    /**
     * Model for [PagingDataSource.state]
     *
     * @param DATA The type of data to be return from the [PageLoader]
     * @property isFirstPageLoading When no [PageLoaderResponse.Success] has
     *     been returned, then this will be true while a request is in flight.
     *     Can be helpful for showing a loading indicator for the whole screen.
     * @property isNextPageLoading When at least one
     *     [PageLoaderResponse.Success] has been returned, then this will
     *     return true while the next page is being loaded. Can be helpful for
     *     knowing when to show a loading indicator at the bottom of the list.
     * @property data A combined list of all the returned pages.
     * @property pageLoaderError An error that can be returned when loading a
     *     page indicating the first or next page. Can be helpful to know if
     *     its the first to show a full screen errror and next page of showing
     *     a footer with a try again button for the user.
     * @property hasMoreToLoad True if there are more pages that can be loaded.
     */
    data class State<DATA>(
        val isFirstPageLoading: Boolean = false,
        val isNextPageLoading: Boolean = false,
        val data: List<DATA> = emptyList(),
        val pageLoaderError: PageLoaderException? = null,
        val hasMoreToLoad: Boolean = false,
    )
}

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