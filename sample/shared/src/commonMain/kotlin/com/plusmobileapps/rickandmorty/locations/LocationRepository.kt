package com.plusmobileapps.rickandmorty.locations

import com.plusmobileapps.rickandmorty.api.RickAndMortyApiClient
import com.plusmobileapps.rickandmorty.api.locations.Location
import com.plusmobileapps.rickandmorty.api.locations.LocationsResponse
import com.plusmobileapps.rickandmorty.db.LocationQueries
import com.plusmobileapps.rickandmorty.db.Locations
import com.plusmobileapps.rickandmorty.util.Dispatchers
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface LocationRepository {
    val hasMoreEpisodesToLoad: Boolean
    val locations: Flow<List<Location>>
    fun loadMore()
    suspend fun getLocation(id: Int): Location?
}

internal class LocationRepositoryImpl(
    private val dispatchers: Dispatchers,
    private val api: RickAndMortyApiClient,
    private val settings: Settings,
    private val db: LocationQueries,
) : LocationRepository {

    companion object {
        const val LOCATIONS_PAGE_KEY = "LOCATIONS_PAGE_KEY"
        const val TOTAL_PAGES_KEY = "LOCATIONS_TOTAL_PAGES_KEY"
    }

    private var nextPage = settings.getInt(LOCATIONS_PAGE_KEY, 1)
        set(value) {
            field = value
            settings[LOCATIONS_PAGE_KEY] = value
        }
    private var totalPages = settings.getInt(TOTAL_PAGES_KEY, Int.MAX_VALUE)
        set(value) {
            field = value
            settings[TOTAL_PAGES_KEY] = value
        }

    private val job = Job()
    private val scope = CoroutineScope(dispatchers.default + job)

    init {
        if (nextPage == 1) loadMore()
    }

    override val hasMoreEpisodesToLoad: Boolean
        get() = nextPage <= totalPages

    override val locations: Flow<List<Location>> =
        db.selectAll()
            .asFlow()
            .mapToList(dispatchers.default)
            .map { locations ->
                locations.map { it.toLocation() }
            }

    override fun loadMore() {
        scope.launch { fetchLocations(nextPage) }
    }

    override suspend fun getLocation(id: Int): Location? =
        db.getLocation(id.toLong())
            .executeAsOneOrNull()
            ?.toLocation()

    private suspend fun fetchLocations(page: Int) {
        if (!hasMoreEpisodesToLoad) return
        try {
            val response: LocationsResponse = api.getLocations(page = page)
            db.transaction {
                response.results.forEach { location ->
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
            nextPage = page + 1
            totalPages = response.info.pages
        } catch (e: Exception) {

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