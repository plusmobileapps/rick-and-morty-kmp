package com.plusmobileapps.paging

import kotlin.test.assertTrue

class PageLoaderMock : PageLoader<String, String> {

    private var mockResponse: () -> PageLoaderResponse<String> = {
        PageLoaderResponse.Error(Exception("Not implemented"))
    }

    private var lastInput: String? = null

    override suspend fun load(request: PageLoaderRequest<String>): PageLoaderResponse<String> {
        lastInput = request.input
        return mockResponse()
    }

    fun everyLoad(mock: () -> PageLoaderResponse<String>) {
        mockResponse = mock
    }

    fun verifyRequestInput(input: String?) {
        assertTrue { input == lastInput }
    }
}