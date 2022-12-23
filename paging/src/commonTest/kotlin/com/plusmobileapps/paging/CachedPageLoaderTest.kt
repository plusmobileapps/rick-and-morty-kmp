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
        ttl = 42.minutes, // meaning of life
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
    fun givenHappyPath_whenPaging_thenStateGetsUpdated() {
        runTest(testDispatcher) {
            val dataSourceTurbine = dataSource.state.testIn(backgroundScope)
            val deleteAllAndWriteTurbine = deleteFlow.testIn(backgroundScope)
            val writerTurbine = writerFlow.testIn(backgroundScope)

            readerFlow.emit(emptyList())
            dataSourceTurbine.awaitItem() shouldBe initialIdleState

            pageLoader.everyLoad {
                PageLoaderResponse.Success(
                    data = listOf(COOL_RICK),
                    pagingToken = FIRST_PAGING_TOKEN,
                )
            }

            dataSource.clearAndLoadFirstPage(INPUT)

            dataSourceTurbine.awaitItem() shouldBe PagingDataSourceState(
                isFirstPageLoading = true,
                isNextPageLoading = false,
                data = emptyList(),
                pageLoaderError = null,
                hasMoreToLoad = true,
            )
            // we want to clear and write with the first page result
            deleteAllAndWriteTurbine.awaitItem() shouldBe listOf(COOL_RICK)
            dataSourceTurbine.awaitItem() shouldBe PagingDataSourceState(
                isFirstPageLoading = false,
                isNextPageLoading = false,
                data = emptyList(), // empty because this gets updated from reader
                pageLoaderError = null,
                hasMoreToLoad = true,
            )

            pageLoader.everyLoad {
                PageLoaderResponse.Success(
                    data = listOf(PICKLE_RICK),
                    pagingToken = null,
                )
            }

            dataSource.loadNextPage()

            dataSourceTurbine.awaitItem() shouldBe PagingDataSourceState(
                isFirstPageLoading = false,
                isNextPageLoading = true,
                data = emptyList(),
                pageLoaderError = null,
                hasMoreToLoad = true,
            )
            writerTurbine.awaitItem() shouldBe listOf(PICKLE_RICK)
            dataSourceTurbine.awaitItem() shouldBe PagingDataSourceState(
                isFirstPageLoading = false,
                isNextPageLoading = false,
                data = emptyList(),
                pageLoaderError = null,
                hasMoreToLoad = false, // null token means there is no more to load
            )
        }
    }

    @Test
    fun whenNewResultsAreEmittedByReader_thenStateShouldUpdateWithLatestResults() {
        runTest(testDispatcher) {
            dataSource.state.test {
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
                readerFlow.emit(listOf(COOL_RICK))
                dataSource.clearAndLoadFirstPage(INPUT)
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
    fun givenFirstPageCacheIsValid_whenLoadingNextPage_thenNextPageShouldLoadWithTheCachedKey() {
        val lastFetchedFirstPageSuccessInstant = nowInstant.minus(40.minutes)
        settings.putString(FIRST_PAGE_SUCCESS_KEY, lastFetchedFirstPageSuccessInstant.toString())
        settings.putString(NEXT_PAGE_PAGING_KEY, FIRST_PAGING_TOKEN)

        runTest(testDispatcher) {
            val dataSourceTurbine = dataSource.state.testIn(backgroundScope)
            val writerTurbine = writerFlow.testIn(backgroundScope)
            readerFlow.emit(listOf(COOL_RICK))
            dataSource.clearAndLoadFirstPage(INPUT)
            dataSourceTurbine.awaitItem() shouldBe PagingDataSourceState(
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
            dataSourceTurbine.awaitItem() shouldBe PagingDataSourceState(
                isFirstPageLoading = false,
                isNextPageLoading = true,
                data = listOf(COOL_RICK),
                pageLoaderError = null,
                hasMoreToLoad = true,
            )
            writerTurbine.awaitItem() shouldBe listOf(PICKLE_RICK)
            pageLoader.verifyPagingToken(FIRST_PAGING_TOKEN)
            dataSourceTurbine.awaitItem() shouldBe PagingDataSourceState(
                isFirstPageLoading = false,
                isNextPageLoading = false,
                data = listOf(COOL_RICK),
                pageLoaderError = null,
                hasMoreToLoad = false,
            )
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