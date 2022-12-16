package com.plusmobileapps.paging

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryPagingLoaderTest {

    private val testDispatcher = StandardTestDispatcher()

    private val konnectivity = KonnectivityMock()

    private val pageLoader = PageLoaderMock()

    val dataSource: InMemoryPageLoader<String, String> by lazy {
        konnectivity.everyIsConnected { true }
        InMemoryPageLoaderImpl(
            ioContext = testDispatcher,
            pageLoader = pageLoader,
            konnectivity = konnectivity,
        )
    }

    @Test
    fun happyPathLoadingTwoPagesUntilNoMoreCanBeLoaded() = runTest(testDispatcher) {
        pageLoader.everyLoad {
            PageLoaderResponse.Success(
                data = listOf(COOL_RICK),
                pagingToken = FIRST_PAGING_TOKEN
            )
        }

        dataSource.state.test {
            awaitItem() shouldBe initialIdleState

            dataSource.clearAndLoadFirstPage(INPUT)

            awaitItem() shouldBe firstPageLoadingState
            awaitItem() shouldBe PagingDataSourceState(
                isFirstPageLoading = false,
                isNextPageLoading = false,
                data = listOf(COOL_RICK),
                pageLoaderError = null,
                hasMoreToLoad = true,
            )

            pageLoader.everyLoad {
                PageLoaderResponse.Success(
                    data = listOf(PICKLE_RICK),
                    pagingToken = null
                )
            }

            dataSource.loadNextPage()

            awaitItem() shouldBe PagingDataSourceState(
                isFirstPageLoading = false,
                isNextPageLoading = true,
                data = listOf(COOL_RICK),
                pageLoaderError = null,
                hasMoreToLoad = true,
            )
            awaitItem() shouldBe PagingDataSourceState(
                isFirstPageLoading = false,
                isNextPageLoading = false,
                data = listOf(COOL_RICK, PICKLE_RICK),
                pageLoaderError = null,
                hasMoreToLoad = false,
            )
        }
    }

    @Test
    fun whenFirstPageLoadingError_thenErrorShouldBeSetForFirstPageAndClearedOutOnNextSuccessfulPageLoad() {
        runTest(testDispatcher) {
            pageLoader.everyLoad {
                PageLoaderResponse.Error(pageRequestException, ERROR_MESSAGE)
            }
            dataSource.state.test {
                awaitItem() shouldBe initialIdleState
                dataSource.clearAndLoadFirstPage(INPUT)
                awaitItem() shouldBe firstPageLoadingState
                awaitItem() shouldBe PagingDataSourceState(
                    isFirstPageLoading = false,
                    isNextPageLoading = false,
                    data = listOf(),
                    pageLoaderError = PageLoaderException.GeneralError(
                        exception = pageRequestException,
                        isFirstPage = true,
                        errorMessage = ERROR_MESSAGE,
                    ),
                    hasMoreToLoad = true,
                )

                pageLoader.everyLoad {
                    PageLoaderResponse.Success(
                        data = listOf(COOL_RICK),
                        pagingToken = FIRST_PAGING_TOKEN,
                    )
                }
                testDispatcher.scheduler.runCurrent()

                dataSource.loadNextPage()

                awaitItem() shouldBe PagingDataSourceState(
                    isFirstPageLoading = true,
                    isNextPageLoading = false,
                    data = listOf(),
                    pageLoaderError = null,
                    hasMoreToLoad = true,
                )
                awaitItem() shouldBe PagingDataSourceState(
                    isFirstPageLoading = false,
                    isNextPageLoading = false,
                    data = listOf(COOL_RICK),
                    pageLoaderError = null,
                    hasMoreToLoad = true,
                )
            }
        }
    }

    @Test
    fun whenNoNetworkOnFirstPageLoad_thenPageLoaderStateShouldHaveNoNetworkException() {
        runTest(testDispatcher) {
            dataSource.state.test {
                konnectivity.everyIsConnected { false }
                dataSource.clearAndLoadFirstPage(INPUT)
                awaitItem() shouldBe initialIdleState
                awaitItem() shouldBe firstPageLoadingState
                awaitItem() shouldBe PagingDataSourceState(
                    isFirstPageLoading = false,
                    isNextPageLoading = false,
                    data = listOf(),
                    pageLoaderError = PageLoaderException.NoNetworkException(isFirstPage = true),
                    hasMoreToLoad = true,
                )

                konnectivity.everyIsConnected { true }
                pageLoader.everyLoad { PageLoaderResponse.Success(listOf(COOL_RICK), "some-token") }
                dataSource.loadNextPage()
                awaitItem() shouldBe firstPageLoadingState
                awaitItem() shouldBe PagingDataSourceState(
                    isFirstPageLoading = false,
                    isNextPageLoading = false,
                    data = listOf(COOL_RICK),
                    pageLoaderError = null,
                    hasMoreToLoad = true,
                )

                konnectivity.everyIsConnected { false }
                dataSource.loadNextPage()
                awaitItem() shouldBe PagingDataSourceState(
                    isFirstPageLoading = false,
                    isNextPageLoading = true,
                    data = listOf(COOL_RICK),
                    pageLoaderError = null,
                    hasMoreToLoad = true,
                )
                awaitItem() shouldBe PagingDataSourceState(
                    isFirstPageLoading = false,
                    isNextPageLoading = false,
                    data = listOf(COOL_RICK),
                    pageLoaderError = PageLoaderException.NoNetworkException(isFirstPage = false),
                    hasMoreToLoad = true,
                )
            }
        }
    }

    companion object {
        const val INPUT = "rick sanchez"
        const val FIRST_PAGING_TOKEN = "first-paging-token"
        const val COOL_RICK = "Cool Rick"
        const val PICKLE_RICK = "Pickle Rick"
        const val ERROR_MESSAGE = "something bad happened"

        val pageRequestException = IllegalStateException()

        val initialIdleState = PagingDataSourceState<String>(
            isFirstPageLoading = false,
            isNextPageLoading = false,
            data = emptyList(),
            pageLoaderError = null,
            hasMoreToLoad = true
        )

        val firstPageLoadingState = PagingDataSourceState<String>(
            isFirstPageLoading = true,
            isNextPageLoading = false,
            data = emptyList(),
            pageLoaderError = null,
            hasMoreToLoad = true,
        )
    }

}