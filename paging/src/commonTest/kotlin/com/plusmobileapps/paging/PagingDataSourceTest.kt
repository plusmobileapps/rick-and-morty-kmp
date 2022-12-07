package com.plusmobileapps.paging

import app.cash.turbine.test
import com.plusmobileapps.konnectivity.Konnectivity
import com.plusmobileapps.konnectivity.NetworkConnection
import com.plusmobileapps.paging.PagingDataSource.State
import com.russhwolf.settings.MapSettings
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class PagingDataSourceTest {

    private val testDispatcher = StandardTestDispatcher()
    private val settings = MapSettings()
    private val nowInstant = Clock.System.now()
    private var cacheInfo: PagingDataSource.CacheInfo? = null

    private val konnectivity = KonnectivityMock()

    private val pageLoader = PageLoaderMock()

    val dataSource: PagingDataSource<String, String> by lazy {
        konnectivity.everyIsConnected { true }
        PagingDataSourceImpl(
            cacheInfo = cacheInfo,
            ioContext = testDispatcher,
            pageLoader = pageLoader,
            konnectivity = konnectivity,
            settings = settings,
            getCurrentInstant = { nowInstant }
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
            awaitItem() shouldBe State(
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

            awaitItem() shouldBe State(
                isFirstPageLoading = false,
                isNextPageLoading = true,
                data = listOf(COOL_RICK),
                pageLoaderError = null,
                hasMoreToLoad = true,
            )
            awaitItem() shouldBe State(
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
                awaitItem() shouldBe State(
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

                awaitItem() shouldBe State(
                    isFirstPageLoading = true,
                    isNextPageLoading = false,
                    data = listOf(),
                    pageLoaderError = null,
                    hasMoreToLoad = true,
                )
                awaitItem() shouldBe State(
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
    fun whenFirstPageCacheIsValid_thenClearAndLoadFirstPageShouldNotTriggerALoad() {
        cacheInfo = PagingDataSource.CacheInfo(
            ttl = 41.minutes,
            cachingKey = "some-key"
        )
        val lastFetchedFirstPageSuccessInstant = nowInstant.minus(40.minutes)
        settings.putString(FIRST_PAGE_SUCCESS_KEY, lastFetchedFirstPageSuccessInstant.toString())

        runTest(testDispatcher) {
            dataSource.state.test {
                dataSource.clearAndLoadFirstPage(INPUT)
                awaitItem() shouldBe State(
                    isFirstPageLoading = false,
                    isNextPageLoading = false,
                    data = listOf(),
                    pageLoaderError = null,
                    hasMoreToLoad = true,
                )

                pageLoader.everyLoad { PageLoaderResponse.Success(listOf(COOL_RICK), "paging-token") }
                dataSource.loadNextPage()
                awaitItem() shouldBe firstPageLoadingState
                awaitItem() shouldBe State(
                    isFirstPageLoading = false,
                    isNextPageLoading = false,
                    data = listOf(COOL_RICK),
                    pageLoaderError = null,
                    hasMoreToLoad = true,
                )
                pageLoader.verifyRequestInput(INPUT)
//                assertEquals(nowInstant.toString(), settings.getStringOrNull(FIRST_PAGE_SUCCESS_KEY))
                assertEquals("paging-token", settings.getStringOrNull(NEXT_PAGE_PAGING_KEY))
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
                awaitItem() shouldBe State(
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
                awaitItem() shouldBe State(
                    isFirstPageLoading = false,
                    isNextPageLoading = false,
                    data = listOf(COOL_RICK),
                    pageLoaderError = null,
                    hasMoreToLoad = true,
                )

                konnectivity.everyIsConnected { false }
                dataSource.loadNextPage()
                awaitItem() shouldBe State(
                    isFirstPageLoading = false,
                    isNextPageLoading = true,
                    data = listOf(COOL_RICK),
                    pageLoaderError = null,
                    hasMoreToLoad = true,
                )
                awaitItem() shouldBe State(
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
        const val FIRST_PAGE_SUCCESS_KEY = "some-key-first-page-caching-key"
        const val NEXT_PAGE_PAGING_KEY = "some-key-next-page-paging-key"
        const val COOL_RICK = "Cool Rick"
        const val PICKLE_RICK = "Pickle Rick"
        const val ERROR_MESSAGE = "something bad happened"

        val pageRequestException = IllegalStateException()

        val initialIdleState = State<String>(
            isFirstPageLoading = false,
            isNextPageLoading = false,
            data = emptyList(),
            pageLoaderError = null,
            hasMoreToLoad = true
        )

        val firstPageLoadingState = State<String>(
            isFirstPageLoading = true,
            isNextPageLoading = false,
            data = emptyList(),
            pageLoaderError = null,
            hasMoreToLoad = true,
        )
    }

}

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

class KonnectivityMock : Konnectivity {

    private var isConnectedMock: () -> Boolean = { false }

    fun everyIsConnected(mock: () -> Boolean) {
        isConnectedMock = mock
    }

    override val currentNetworkConnection: NetworkConnection
        get() = TODO("Not yet implemented")
    override val currentNetworkConnectionState: StateFlow<NetworkConnection>
        get() = TODO("Not yet implemented")
    override val isConnected: Boolean
        get() = isConnectedMock()
    override val isConnectedState: StateFlow<Boolean>
        get() = TODO("Not yet implemented")
}