package com.plusmobileapps.paging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Model for a paging data source such as [InMemoryPageLoader] or
 * [CachedPageLoader].
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
data class PagingDataSourceState<DATA>(
    val isFirstPageLoading: Boolean = false,
    val isNextPageLoading: Boolean = false,
    val data: List<DATA> = emptyList(),
    val pageLoaderError: PageLoaderException? = null,
    val hasMoreToLoad: Boolean = false,
)

internal data class State<INPUT, DATA>(
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

    val hasMoreToLoad: Boolean
        get() = (pageLoaderState as? PageLoaderState.Idle)?.hasMorePages == true
                || (pageLoaderState as? PageLoaderState.Failed)?.exception?.isFirstPage == true
                || pageLoaderState is PageLoaderState.Loading
                || pagingKey != null
}

internal fun <DATA> State<*, DATA>.toPagingDataSourceState() = PagingDataSourceState(
    isFirstPageLoading = firstPageIsLoading,
    isNextPageLoading = nextPageIsLoading,
    data = data,
    pageLoaderError = (pageLoaderState as? PageLoaderState.Failed)?.exception,
    hasMoreToLoad = hasMoreToLoad
)