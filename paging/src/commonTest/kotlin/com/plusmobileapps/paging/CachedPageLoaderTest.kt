package com.plusmobileapps.paging

import app.cash.turbine.test
import com.russhwolf.settings.MapSettings
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class CachedPageLoaderTest {

    private val testDispatcher = StandardTestDispatcher()
    private val cacheInfo = CachedPageLoader.CacheInfo(
        ttl = 41.minutes,
        cachingKey = "some-key"
    )
    private val settings = MapSettings()
    private val nowInstant = Clock.System.now()
    private val konnectivity = KonnectivityMock()
    private val pageLoader = PageLoaderMock()

    private val dataSource: CachedPageLoader<String, String> by lazy {
        CachedPageLoaderImpl(
            cacheInfo = cacheInfo,
            ioContext = Dispatchers.Default,
            reader = { flowOf(emptyList()) },
            writer = { },
            deleteAllAndWrite = { },
            pageLoader = pageLoader,
            konnectivity = konnectivity,
            settings = settings,
            getCurrentInstant = { Clock.System.now() },
        )
    }

    @Test
    fun whenFirstPageCacheIsValid_thenClearAndLoadFirstPageShouldNotTriggerALoad() {
        val lastFetchedFirstPageSuccessInstant = nowInstant.minus(40.minutes)
        settings.putString(InMemoryPagingLoaderTest.FIRST_PAGE_SUCCESS_KEY, lastFetchedFirstPageSuccessInstant.toString())

        runTest(testDispatcher) {
            dataSource.state.test {
                dataSource.clearAndLoadFirstPage(InMemoryPagingLoaderTest.INPUT)
                awaitItem() shouldBe PagingDataSourceState(
                    isFirstPageLoading = false,
                    isNextPageLoading = false,
                    data = listOf(),
                    pageLoaderError = null,
                    hasMoreToLoad = true,
                )

                pageLoader.everyLoad { PageLoaderResponse.Success(listOf(InMemoryPagingLoaderTest.COOL_RICK), "paging-token") }
                dataSource.loadNextPage()
                awaitItem() shouldBe InMemoryPagingLoaderTest.firstPageLoadingState
                awaitItem() shouldBe PagingDataSourceState(
                    isFirstPageLoading = false,
                    isNextPageLoading = false,
                    data = listOf(InMemoryPagingLoaderTest.COOL_RICK),
                    pageLoaderError = null,
                    hasMoreToLoad = true,
                )
                pageLoader.verifyRequestInput(InMemoryPagingLoaderTest.INPUT)
//                assertEquals(nowInstant.toString(), settings.getStringOrNull(FIRST_PAGE_SUCCESS_KEY))
                assertEquals("paging-token", settings.getStringOrNull(InMemoryPagingLoaderTest.NEXT_PAGE_PAGING_KEY))
            }
        }
    }
}