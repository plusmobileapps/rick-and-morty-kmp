package com.plusmobileapps.paging

/**
 * A type alias for a function that makes an async call with
 * [PageLoaderRequest] and returns a [PageLoaderResponse]. This is then
 * passed as a parameter for [PagingDataSource.Factory.create] and will be
 * where one typically fetches data from an API.
 */
interface PageLoader<INPUT, DATA> {
    suspend fun load(request: PageLoaderRequest<INPUT>): PageLoaderResponse<DATA>
}

/** An error that can happen when loading a page. */
sealed class PageLoaderException(message: String?) : Exception(message) {
    abstract val isFirstPage: Boolean

    data class NoNetworkException(
        override val isFirstPage: Boolean,
    ) : PageLoaderException("No internet connection available to load page request")

    data class GeneralError(
        val exception: Exception,
        override val isFirstPage: Boolean,
        private val errorMessage: String?,
    ) : PageLoaderException(errorMessage)
}

/** Model for the state of the current page loader. */
internal sealed class PageLoaderState {

    /**
     * The page loader has either just been initialized or has finished the
     * last page load request.
     *
     * @property hasMorePages True if there are more pages that could be
     *     loaded. Can be helpful to show a message to the user that they have
     *     reached the end of the list.
     */
    data class Idle(val hasMorePages: Boolean) : PageLoaderState()

    /**
     * A page loading request is in flight.
     *
     * @property isFirstPage True if this is the first page request loading.
     */
    data class Loading(val isFirstPage: Boolean) : PageLoaderState()

    /**
     * The last page load request has failed.
     *
     * @property exception
     */
    data class Failed(val exception: PageLoaderException) : PageLoaderState()
}

/**
 * Page loader request model.
 *
 * @param INPUT The type of input for each request.
 * @property pagingKey The key for the next page to be loaded, null if the
 *     first page.
 * @property input The input to be used for each page load request.
 */
data class PageLoaderRequest<INPUT>(
    val pagingKey: String? = null,
    val input: INPUT,
)

/**
 * Page loader response model.
 *
 * @param DATA The type of data being returned for every page loader
 *     response.
 */
sealed class PageLoaderResponse<out DATA> {

    /**
     * A successful response from a page loading request.
     *
     * @property data The results returned from the [PageLoaderRequest].
     * @property pagingToken The next paging token to be used on the next
     *     request when [PagingDataSource.loadNextPage] is called. Return null
     *     if there are no more pages to load.
     */
    data class Success<DATA>(
        val data: List<DATA>,
        val pagingToken: String? = null,
    ) : PageLoaderResponse<DATA>()

    /**
     * An error occuring when making a [PageLoaderRequest].
     *
     * @property exception
     * @property message
     */
    data class Error(
        val exception: Exception,
        val message: String? = null,
    ) : PageLoaderResponse<Nothing>()
}
