package com.plusmobileapps.paging

/**
 * A type alias for a function that makes an async call with [PageLoaderRequest] and returns
 * a [PageLoaderResponse]. This is then passed as a parameter for [PagingDataSource.Factory.create]
 * and will be where one typically fetches data from an API.
 */
typealias PageLoader<INPUT, DATA> = suspend (PageLoaderRequest<INPUT>) -> PageLoaderResponse<DATA>

sealed class PageLoaderError {
    data class FirstPage(val exception: Exception, val message: String? = null) : PageLoaderError()
    data class NextPage(val exception: Exception, val message: String?) : PageLoaderError()
}

internal sealed class PageLoaderState {
    data class Idle(val hasMorePages: Boolean) : PageLoaderState()
    data class Loading(val isFirstPage: Boolean) : PageLoaderState()
    data class Failed(
        val exception: Exception,
        val message: String?,
        val isFirstPage: Boolean
    ) : PageLoaderState()
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
