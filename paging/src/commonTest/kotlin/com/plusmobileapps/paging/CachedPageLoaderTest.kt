package com.plusmobileapps.paging

import app.cash.turbine.test
import app.cash.turbine.testIn
import com.russhwolf.settings.MapSettings
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
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
    private val readerFlow = MutableSharedFlow<List<String>>()
    private val writerFlow = MutableSharedFlow<List<String>>()
    private val deleteFlow = MutableSharedFlow<List<String>>()
    private val settings = MapSettings()
    private val nowInstant = Clock.System.now()
    private val konnectivity = KonnectivityMock()
    private val pageLoader = PageLoaderMock()

    private val dataSource: CachedPageLoader<String, String> by lazy {
        konnectivity.everyIsConnected { true }
        CachedPageLoaderImpl(
            cacheInfo = cacheInfo,
            ioContext = Dispatchers.Default,
            reader = { readerFlow },
            writer = { writerFlow.emit(it) },
            deleteAllAndWrite = { deleteFlow.emit(it) },
            pageLoader = pageLoader,
            konnectivity = konnectivity,
            settings = settings,
            getCurrentInstant = { nowInstant },
        )
    }

    @Test
    fun whenNewResultsAreCollectedByReader_thenStateShouldUpdateWithLatestResults() {
        runTest(testDispatcher) {
            dataSource.state.test {
                awaitItem() shouldBe initialIdleState

                readerFlow.emit(listOf(COOL_RICK))
                awaitItem() shouldBe PagingDataSourceState(
                    isFirstPageLoading = false,
                    isNextPageLoading = false,
                    data = listOf(COOL_RICK),
                    pageLoaderError = null,
                    hasMoreToLoad = true,
                )

                readerFlow.emit(listOf(COOL_RICK, PICKLE_RICK))
                awaitItem() shouldBe PagingDataSourceState(
                    isFirstPageLoading = false,
                    isNextPageLoading = false,
                    data = listOf(COOL_RICK, PICKLE_RICK),
                    pageLoaderError = null,
                    hasMoreToLoad = true,
                )
            }
        }
    }

    @Test
    fun whenFirstPageCacheIsValid_thenClearAndLoadFirstPageShouldNotTriggerALoad() {
        val lastFetchedFirstPageSuccessInstant = nowInstant.minus(40.minutes)
        settings.putString(FIRST_PAGE_SUCCESS_KEY, lastFetchedFirstPageSuccessInstant.toString())

        runTest(testDispatcher) {
            dataSource.state.test {
                dataSource.clearAndLoadFirstPage(INPUT)
                awaitItem() shouldBe initialIdleState
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