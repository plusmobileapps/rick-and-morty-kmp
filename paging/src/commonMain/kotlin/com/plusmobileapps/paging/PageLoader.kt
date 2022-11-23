package com.plusmobileapps.paging

typealias PageLoader<INPUT, DATA> = suspend (PageLoaderRequest<INPUT>) -> PageLoaderResponse<DATA>

data class PageLoaderData<DATA>(val pageLoaderState: PageLoaderState, val data: List<DATA>)

sealed class PageLoaderState {
    data class Idle(val hasMorePages: Boolean) : PageLoaderState()
    data class Loading(val isFirstPage: Boolean) : PageLoaderState()
    data class Failed(val canRetrySameRequest: Boolean, val message: String?) : PageLoaderState()
}

data class PageLoaderRequest<INPUT>(
    val pagingKey: String? = null,
    val pageSize: Int,
    val input: INPUT,
)

sealed class PageLoaderResponse<out DATA> {
    data class Success<DATA>(
        val data: List<DATA>,
        val pagingToken: String? = null,
    ) : PageLoaderResponse<DATA>()

    data class Error(
        val canRetrySameRequest: Boolean,
        val message: String? = null,
    ) : PageLoaderResponse<Nothing>()
}
