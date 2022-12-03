package com.plusmobileapps.paging

/**
 * A type alias for a function that makes an async call with
 * [PageLoaderRequest] and returns a [PageLoaderResponse]. This is then
 * passed as a parameter for [PagingDataSource.Factory.create] and will be
 * where one typically fetches data from an API.
 */
typealias PageLoader<INPUT, DATA> = suspend (PageLoaderRequest<INPUT>) -> PageLoaderResponse<DATA>

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

internal sealed class PageLoaderState {
    data class Idle(val hasMorePages: Boolean) : PageLoaderState()
    data class Loading(val isFirstPage: Boolean) : PageLoaderState()
    data class Failed(val exception: PageLoaderException) : PageLoaderState()
}

data class PageLoaderRequest<INPUT>(
    val pagingKey: String? = null,
    val input: INPUT,
)

sealed class PageLoaderResponse<out DATA> {
    data class Success<DATA>(
        val data: List<DATA>,
        val pagingToken: String? = null,
    ) : PageLoaderResponse<DATA>()

    data class Error(
        val exception: Exception,
        val message: String? = null,
    ) : PageLoaderResponse<Nothing>()
}
