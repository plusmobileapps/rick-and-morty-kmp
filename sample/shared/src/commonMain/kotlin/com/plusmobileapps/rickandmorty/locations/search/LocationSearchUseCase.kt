package com.plusmobileapps.rickandmorty.locations.search

import com.plusmobileapps.paging.*
import com.plusmobileapps.rickandmorty.api.RickAndMortyApiClient
import com.plusmobileapps.rickandmorty.api.locations.Location
import com.plusmobileapps.rickandmorty.locations.search.LocationSearchUseCaseImpl.Input
import kotlinx.coroutines.flow.Flow

interface LocationSearchUseCase {
    val pageLoaderState: Flow<PagingDataSourceState<Location>>
    suspend fun clearAndLoadFirstPage(
        location: String,
        type: String,
        dimension: String,
    )
    suspend fun loadNextPage()
}

internal class LocationSearchUseCaseImpl(
    private val api: RickAndMortyApiClient,
    factory: InMemoryPageLoader.Factory,
) : LocationSearchUseCase, PageLoader<Input, Location> {

    private val pagingSource: InMemoryPageLoader<Input, Location> = factory.create(this)

    override val pageLoaderState: Flow<PagingDataSourceState<Location>>
        get() = pagingSource.state

    override suspend fun clearAndLoadFirstPage(location: String, type: String, dimension: String) {
        pagingSource.clearAndLoadFirstPage(
            Input(
                location = location,
                type = type.takeIf { it.isNotBlank() },
                dimension = dimension.takeIf { it.isNotBlank() },
            )
        )
    }

    override suspend fun loadNextPage() {
        pagingSource.loadNextPage()
    }

    override suspend fun load(request: PageLoaderRequest<Input>): PageLoaderResponse<Location> {
        return try {
            val response = api.getLocations(
                page = request.pagingKey?.toIntOrNull() ?: 1,
                name = request.input.location,
                type = request.input.type,
                dimension = request.input.dimension,
            )
            PageLoaderResponse.Success(response.results, response.info.nextPageNumber)
        } catch (e: Exception) {
            PageLoaderResponse.Error(e)
        }
    }

    internal data class Input(
        val location: String,
        val type: String?,
        val dimension: String?,
    )
}