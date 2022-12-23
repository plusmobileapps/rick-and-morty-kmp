package com.plusmobileapps.rickandmorty.episodes.search

import com.plusmobileapps.paging.*
import com.plusmobileapps.rickandmorty.api.RickAndMortyApiClient
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import kotlinx.coroutines.flow.Flow

interface EpisodeSearchUseCase {

    val pageLoaderState: Flow<PagingDataSourceState<Episode>>

    suspend fun loadFirstPage(
        name: String,
        episodeCode: String?
    )

    suspend fun loadNextPage()
}

internal class EpisodeSearchUseCaseImpl(
    private val api: RickAndMortyApiClient,
    factory: InMemoryPageLoader.Factory,
): EpisodeSearchUseCase {

    private val pageLoader: InMemoryPageLoader<Input, Episode> = factory.create(
        object : PageLoader<Input, Episode> {
            override suspend fun load(request: PageLoaderRequest<Input>): PageLoaderResponse<Episode> {
                return try {
                    val response = api.getEpisodes(
                        page = request.pagingKey?.toIntOrNull() ?: 0,
                        name = request.input.name,
                        episodeCode = request.input.episodeCode,
                    )
                    PageLoaderResponse.Success(response.results, response.info.nextPageNumber)
                } catch (e: Exception) {
                    PageLoaderResponse.Error(e)
                }
            }
        }
    )

    override val pageLoaderState: Flow<PagingDataSourceState<Episode>> = pageLoader.state

    override suspend fun loadFirstPage(name: String, episodeCode: String?) {
        pageLoader.clearAndLoadFirstPage(Input(name, episodeCode))
    }

    override suspend fun loadNextPage() {
        pageLoader.loadNextPage()
    }

    data class Input(val name: String, val episodeCode: String?)
}