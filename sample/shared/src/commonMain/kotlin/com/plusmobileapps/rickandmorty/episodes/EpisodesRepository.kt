package com.plusmobileapps.rickandmorty.episodes

import com.plusmobileapps.paging.*
import com.plusmobileapps.rickandmorty.api.RickAndMortyApiClient
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.api.episodes.EpisodesResponse
import com.plusmobileapps.rickandmorty.db.EpisodeQueries
import com.plusmobileapps.rickandmorty.db.Episodes
import com.plusmobileapps.rickandmorty.util.Dispatchers
import com.squareup.sqldelight.TransactionWithoutReturn
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.days

interface EpisodesRepository {
    val pagingState: Flow<PagingDataSourceState<Episode>>
    suspend fun loadFirstPage()
    suspend fun loadNextPage()
    fun getEpisodes(): Flow<List<Episode>>
    suspend fun getEpisode(id: Int): Episode
}

internal class EpisodesRepositoryImpl(
    private val api: RickAndMortyApiClient,
    private val dispatchers: Dispatchers,
    private val db: EpisodeQueries,
    pagingFactory: CachedPageLoader.Factory,
) : EpisodesRepository, PageLoader<Unit, Episode> {

    private val pagingDataSource: CachedPageLoader<Unit, Episode> = pagingFactory.create(
        cacheInfo = CachedPageLoader.CacheInfo(
            ttl = 1.days,
            cachingKey = "episodes-caching-key",
        ),
        reader = { getEpisodes() },
        writer = { db.transaction { insertEpisodes(it) } },
        deleteAllAndWrite = { episodes ->
            db.transaction {
                db.deleteAll()
                insertEpisodes(episodes)
            }
        },
        pageLoader = this,
    )

    override val pagingState: Flow<PagingDataSourceState<Episode>>
        get() = pagingDataSource.state

    override suspend fun loadFirstPage() {
        pagingDataSource.clearAndLoadFirstPage(Unit)
    }

    override suspend fun loadNextPage() {
        pagingDataSource.loadNextPage()
    }

    override fun getEpisodes(): Flow<List<Episode>> =
        db.selectAll()
            .asFlow()
            .mapToList(dispatchers.default)
            .map { episodes ->
                episodes.map { it.toEpisode() }
            }

    override suspend fun getEpisode(id: Int): Episode = withContext(dispatchers.default) {
        db.getEpisode(id.toLong()).executeAsOne().toEpisode()
    }

    override suspend fun load(request: PageLoaderRequest<Unit>): PageLoaderResponse<Episode> {
        return try {
            val response: EpisodesResponse = api.getEpisodes(request.pagingKey?.toIntOrNull() ?: 0)
            PageLoaderResponse.Success(response.results, response.info.nextPageNumber)
        } catch (e: Exception) {
            PageLoaderResponse.Error(e)
        }
    }

    private fun TransactionWithoutReturn.insertEpisodes(episodes: List<Episode>) {
        episodes.forEach { episode ->
            db.insertEpisode(
                id = episode.id.toLong(),
                name = episode.name,
                air_date = episode.air_date,
                episode = episode.episode,
                characters = episode.characters.joinToString(","),
                url = episode.url,
                created = episode.created
            )
        }
    }

    private fun Episodes.toEpisode(): Episode =
        Episode(
            id = id.toInt(),
            name = name,
            air_date = air_date,
            episode = episode,
            characters = characters.split(",").mapNotNull { character ->
                character.takeIf { it.isNotBlank() }
            },
            url = url,
            created = created
        )
}