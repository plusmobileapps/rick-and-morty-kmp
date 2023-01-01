package com.plusmobileapps.rickandmorty.locations

import com.plusmobileapps.paging.*
import com.plusmobileapps.rickandmorty.api.RickAndMortyApiClient
import com.plusmobileapps.rickandmorty.api.locations.Location
import com.plusmobileapps.rickandmorty.api.locations.LocationsResponse
import com.plusmobileapps.rickandmorty.db.LocationQueries
import com.plusmobileapps.rickandmorty.db.Locations
import com.plusmobileapps.rickandmorty.util.Dispatchers
import com.squareup.sqldelight.TransactionWithoutReturn
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.days

interface LocationRepository {
    val pageLoaderState: Flow<PagingDataSourceState<Location>>
    val locations: Flow<List<Location>>
    suspend fun loadFirstPage()
    suspend fun loadNextPage()
    suspend fun getLocation(id: Int): Location?
}

internal class LocationRepositoryImpl(
    private val dispatchers: Dispatchers,
    private val api: RickAndMortyApiClient,
    private val db: LocationQueries,
    factory: CachedPageLoader.Factory,
) : LocationRepository, PageLoader<Unit, Location> {

    private val pagingSource: CachedPageLoader<Unit, Location> = factory.create(
        cacheInfo = CachedPageLoader.CacheInfo(
            ttl = 1.days,
            cachingKey = "locations-repository"
        ),
        reader = { locations },
        writer = { db.transaction { insertLocations(it) } },
        deleteAllAndWrite = {
            db.transaction {
                db.deleteAll()
                insertLocations(it)
            }
        },
        pageLoader = this,
    )

    override val pageLoaderState: Flow<PagingDataSourceState<Location>>
        get() = pagingSource.state

    override suspend fun loadFirstPage() {
        pagingSource.loadFirstPage(Unit)
    }

    override suspend fun loadNextPage() {
        pagingSource.loadNextPage()
    }

    override val locations: Flow<List<Location>>
        get() = db.selectAll()
            .asFlow()
            .mapToList(dispatchers.default)
            .map { locations ->
                locations.map { it.toLocation() }
            }

    override suspend fun getLocation(id: Int): Location? =
        db.getLocation(id.toLong())
            .executeAsOneOrNull()
            ?.toLocation()

    override suspend fun load(request: PageLoaderRequest<Unit>): PageLoaderResponse<Location> {
        return try {
            val response: LocationsResponse =
                api.getLocations(page = request.pagingKey?.toIntOrNull() ?: 1)
            PageLoaderResponse.Success(
                data = response.results,
                pagingToken = response.info.nextPageNumber,
            )
        } catch (e: Exception) {
            PageLoaderResponse.Error(e)
        }
    }

    private fun TransactionWithoutReturn.insertLocations(locations: List<Location>) {
        locations.forEach { location ->
            db.insertLocation(
                id = location.id.toLong(),
                name = location.name,
                type = location.type,
                dimension = location.dimension,
                residents = location.residents.joinToString(separator = ","),
                url = location.url,
                created = location.created
            )
        }
    }
}

private fun Locations.toLocation(): Location = Location(
    id = id.toInt(),
    name = name,
    type = type,
    dimension = dimension,
    residents = residents.split(",")
        .mapNotNull { character -> character.takeIf { it.isNotBlank() } },
    url = url,
    created = created
)