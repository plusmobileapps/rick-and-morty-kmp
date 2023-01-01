package com.plusmobileapps.paging

import kotlinx.coroutines.flow.Flow

/**
 * A data source that can load pages and manage the state of loading pages
 * contained within itself. Best suited for use cases that do not need to
 * be backed by a database as it will handle the paging state in memory,
 * such as performing a search on an API with changing query parameters.
 *
 * @param INPUT The type of input to be used on a [PageLoaderRequest].
 * @param DATA The type of data to be return from [PageLoaderResponse].
 */
interface InMemoryPageLoader<INPUT, DATA> {

    /** A [Flow] for the current state of the [InMemoryPageLoader]. */
    val state: Flow<PagingDataSourceState<DATA>>

    /**
     * Will clear all results and load the first page with the provided input.
     *
     * @param input The input to be used in each [PageLoaderRequest.input].
     */
    suspend fun clearAndLoadFirstPage(input: INPUT)

    /**
     * Load the next page with the same input provided from the first page if
     * one exists.
     */
    suspend fun loadNextPage()

    /** A factory to create an instance of a [InMemoryPageLoader]. */
    interface Factory {

        /**
         * Create an instance of a [InMemoryPageLoader] by providing a lambda that
         * can make page loader request.
         *
         * @param pageLoader
         * @param cacheInfo If set, will enable caching logic to only fetch new
         *     pages or unfetched pages if the cache is no longer valid.
         * @param INPUT The input used for loading all pages on the request.
         * @param DATA The results returned in a [PageLoaderResponse].
         * @return
         */
        fun <INPUT, DATA> create(
            pageLoader: PageLoader<INPUT, DATA>,
        ): InMemoryPageLoader<INPUT, DATA>
    }
}