package com.plusmobileapps.paging

import app.cash.turbine.test
import com.plusmobileapps.konnectivity.Konnectivity
import com.plusmobileapps.paging.PagingDataSource.State
import com.russhwolf.settings.MapSettings
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PagingDataSourceTest : TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    private val testDispatcher = StandardTestDispatcher()
    private val settings = MapSettings()
    private val nowInstant = Clock.System.now()

    @Mock
    lateinit var konnectivity: Konnectivity

    @Mock
    lateinit var pageLoader: PageLoader<String, String>

    val dataSource: PagingDataSource<String, String> by withMocks {
        every { konnectivity.isConnected } returns true
        PagingDataSourceImpl(
            cacheInfo = null,
            ioContext = testDispatcher,
            pageLoader = pageLoader,
            konnectivity = konnectivity,
            settings = settings,
            getCurrentInstant = { nowInstant }
        )
    }

    @Test
    fun happyPathLoadingTwoPagesUntilNoMoreCanBeLoaded() = runTest(testDispatcher) {
        mockRequest(INPUT, pagingKey = null) {
            PageLoaderResponse.Success(
                data = listOf(COOL_RICK),
                pagingToken = FIRST_PAGING_TOKEN
            )
        }

        mockRequest(INPUT, pagingKey = FIRST_PAGING_TOKEN) {
            PageLoaderResponse.Success(
                data = listOf(PICKLE_RICK),
                pagingToken = null
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

    private suspend fun mockRequest(
        input: String,
        pagingKey: String? = null,
        response: () -> PageLoaderResponse<String>
    ) {
        everySuspending {
            pageLoader.load(
                PageLoaderRequest(
                    pagingKey = pagingKey,
                    input = input
                ))
        } returns response()
    }

    companion object {
        const val INPUT = "rick sanchez"
        const val FIRST_PAGING_TOKEN = "first-paging-token"
        const val COOL_RICK = "Cool Rick"
        const val PICKLE_RICK = "Pickle Rick"

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