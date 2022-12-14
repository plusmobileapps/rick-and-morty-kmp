package com.plusmobileapps.paging

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

/**
 * A data source that can load pages and manages the state by delegating
 * to an external source of truth. Best suited for use inside a repository
 * when caching to a database is needed.
 *
 * @param INPUT The type of input to be used on a [PageLoaderRequest].
 * @param DATA The type of data to be return from [PageLoaderResponse].
 */
interface CachedPageLoader<INPUT, DATA> {

    /** A [Flow] for the current state of the [CachedPageLoader]. */
    val state: Flow<PagingDataSourceState<DATA>>

    /**
     * Will attempt to load the first page if the cache is invalid and can
     * trigger a request to delete and save the new results that are returned
     * to the backing cache. This must always be called first before trying to
     * load the next page to ensure the input is set.
     *
     * @param input The input to be used in each [PageLoaderRequest.input].
     */
    suspend fun loadFirstPage(input: INPUT)

    /**
     * Load the next page with the same input provided from the first page
     * if one exists. If the cache is still valid, it will continue loading
     * the next page from the last successful paging token that was returned.
     */
    suspend fun loadNextPage()

    /** A factory to create an instance of [CachedPageLoader]. */
    interface Factory {

        /**
         * Create an instance of a [CachedPageLoader] by providing lambdas to make
         * the api requests and interact with the database.
         *
         * @param cacheInfo The info for how long the cache should live.
         * @param reader Lambda that returns a [Flow] to observe [DATA] changing in
         *     the database.
         * @param writer Lambda that can write [DATA] into the database when
         *     results are returned from a [PageLoader.load].
         * @param deleteAllAndWrite Lambda that can delete all entries in the
         *     database once the cache is invalid and will then write the results
         *     within the same database transaction.
         * @param pageLoader What is making the requests to the API.
         * @param INPUT The input used for loading all pages on the request.
         * @param DATA The results returned in a [PageLoaderResponse].
         * @return
         */
        fun <INPUT, DATA> create(
            cacheInfo: CacheInfo,
            reader: () -> Flow<List<DATA>>,
            writer: suspend (List<DATA>) -> Unit,
            deleteAllAndWrite: suspend (List<DATA>) -> Unit,
            pageLoader: PageLoader<INPUT, DATA>,
        ): CachedPageLoader<INPUT, DATA>
    }

    /**
     * Model for representing the info for how the [CachedPageLoader] should
     * handle caching or trigger a refresh on the first page.
     *
     * @property ttl The time to live for a valid cache.
     * @property cachingKey A unique key for the [CachedPageLoader].
     */
    data class CacheInfo(
        val ttl: Duration,
        val cachingKey: String,
    )

}