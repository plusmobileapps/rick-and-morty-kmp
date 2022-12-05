package com.plusmobileapps.paging

import app.cash.turbine.test
import com.plusmobileapps.konnectivity.Konnectivity
import com.plusmobileapps.paging.PagingDataSource.State
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
    fun testTheThings() = runTest(testDispatcher) {
        everySuspending {
            pageLoader.load(
                PageLoaderRequest(
                    pagingKey = null,
                    input = INPUT
                )
            )
        } returns PageLoaderResponse.Success(
            data = listOf(CHARACTER_ONE),
            pagingToken = FIRST_PAGING_TOKEN
        )

        dataSource.state.test {
            assertEquals(
                State(
                    isFirstPageLoading = false,
                    isNextPageLoading = false,
                    data = emptyList(),
                    pageLoaderError = null, hasMoreToLoad = true
                ),
                awaitItem()
            )
            dataSource.clearAndLoadFirstPage(INPUT)
            assertEquals(
                State(
                    isFirstPageLoading = true,
                    isNextPageLoading = false,
                    data = emptyList(),
                    pageLoaderError = null,
                    hasMoreToLoad = false,
                ),
                awaitItem()
            )
            assertEquals(
                State(
                    isFirstPageLoading = false,
                    isNextPageLoading = false,
                    data = listOf(CHARACTER_ONE),
                    pageLoaderError = null,
                    hasMoreToLoad = true,
                ),
                awaitItem()
            )
        }
    }

    companion object {
        const val INPUT = "rick sanchez"
        const val FIRST_PAGING_TOKEN = "first-paging-token"
        const val CHARACTER_ONE = "Cool Rick"
    }

}