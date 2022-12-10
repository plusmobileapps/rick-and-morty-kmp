package com.plusmobileapps.paging

import com.plusmobileapps.konnectivity.Konnectivity
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

object PagingDataSourceFactory : InMemoryPageLoader.Factory, CachedPageLoader.Factory {
    private val konnectivity = Konnectivity()
    private val settings = Settings()

    override fun <INPUT, DATA> create(
        pageLoader: PageLoader<INPUT, DATA>
    ): InMemoryPageLoader<INPUT, DATA> {
        return InMemoryPageLoaderImpl(
            ioContext = Dispatchers.Default,
            pageLoader = pageLoader,
            konnectivity = konnectivity,
        )
    }

    override fun <INPUT, DATA> create(
        cacheInfo: CachedPageLoader.CacheInfo,
        reader: () -> Flow<List<DATA>>,
        writer: suspend (List<DATA>) -> Unit,
        deleteAllAndWrite: suspend (List<DATA>) -> Unit,
        pageLoader: PageLoader<INPUT, DATA>
    ): CachedPageLoader<INPUT, DATA> {
        return CachedPageLoaderImpl<INPUT, DATA>(
            cacheInfo = cacheInfo,
            ioContext = Dispatchers.Default,
            reader = reader,
            writer = writer,
            deleteAllAndWrite = deleteAllAndWrite,
            pageLoader = pageLoader,
            konnectivity = konnectivity,
            settings = settings,
            getCurrentInstant = { Clock.System.now() },
        )
    }
}