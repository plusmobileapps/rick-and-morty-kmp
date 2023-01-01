package com.plusmobileapps.paging

import kotlin.test.assertTrue

class PageLoaderMock : PageLoader<String, String> {

    private var mockResponse: () -> PageLoaderResponse<String> = {
        PageLoaderResponse.Error(Exception("Not implemented"))
    }

    private var lastInput: String? = null
    private var lastPagingToken: String? = null

    override suspend fun load(request: PageLoaderRequest<String>): PageLoaderResponse<String> {
        lastInput = request.input
        lastPagingToken = request.pagingKey
        return mockResponse()
    }

    fun everyLoad(mock: () -> PageLoaderResponse<String>) {
        mockResponse = mock
    }

    fun verifyRequestInput(input: String?) {
        assertTrue { input == lastInput }
    }

    fun verifyPagingToken(pagingToken: String?) {
        assertTrue { lastPagingToken == pagingToken }
    }
}