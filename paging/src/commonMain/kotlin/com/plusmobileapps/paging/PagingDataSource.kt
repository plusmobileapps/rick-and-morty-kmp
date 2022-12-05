package com.plusmobileapps.paging

import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

/**
 * A data source that can load pages and manage the state of loading pages.
 *
 * @param INPUT The type of input to be used on a [PageLoaderRequest].
 * @param DATA The type of data to be return from [PageLoaderResponse].
 */
interface PagingDataSource<INPUT, DATA> {

    /** A [StateFlow] for the current state of the [PagingDataSource]. */
    val state: StateFlow<State<DATA>>

    /**
     * Will clear all results and load the first page with the provided input.
     *
     * @param input The input to be used in each [PageLoaderRequest.input].
     */
    fun clearAndLoadFirstPage(input: INPUT)

    /**
     * Load the next page with the same input provided from the first page if
     * one exists.
     */
    fun loadNextPage()

    /** A factory to create an instance of a [PagingDataSource]. */
    interface Factory {

        /**
         * Create an instance of a [PagingDataSource] by providing a lambda that
         * can make page loader request.
         *
         * @param cacheInfo If set, will enable caching logic to only fetch new
         *     pages or unfetched pages if the cache is no longer valid.
         * @param pageLoader
         * @param INPUT The input used for loading all pages on the request.
         * @param DATA The results returned in a [PageLoaderResponse].
         * @return
         */
        fun <INPUT, DATA> create(
            cacheInfo: CacheInfo? = null,
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

    data class CacheInfo(
        val ttl: Duration,
        val cachingKey: String,
    )
}