package com.plusmobileapps.rickandmorty.episodes

import com.plusmobileapps.rickandmorty.api.RickAndMortyApiClient
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.api.episodes.EpisodesResponse
import com.plusmobileapps.rickandmorty.db.EpisodeQueries
import com.plusmobileapps.rickandmorty.db.Episodes
import com.plusmobileapps.rickandmorty.util.Dispatchers
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface EpisodesRepository {
    val hasMoreEpisodesToLoad: Boolean
    fun loadNextPage()
    fun getEpisodes(): Flow<List<Episode>>
    suspend fun getEpisode(id: Int): Episode
}

internal class EpisodesRepositoryImpl(
    private val api: RickAndMortyApiClient,
    private val dispatchers: Dispatchers,
    private val db: EpisodeQueries,
    private val settings: Settings,
) : EpisodesRepository {

    companion object {
        const val EPISODES_PAGE_KEY = "EPISODES_PAGE_KEY"
        const val TOTAL_PAGES_KEY = "EPISODES_TOTAL_PAGES_KEY"
    }

    private var nextPage = settings.getInt(EPISODES_PAGE_KEY, 1)
        set(value) {
            field = value
            settings[EPISODES_PAGE_KEY] = value
        }
    private var totalPages = settings.getInt(TOTAL_PAGES_KEY, Int.MAX_VALUE)
        set(value) {
            field = value
            settings[TOTAL_PAGES_KEY] = value
        }

    private val job = Job()
    private val scope = CoroutineScope(dispatchers.default + job)

    init {
        if (nextPage == 1) loadNextPage()
    }

    override val hasMoreEpisodesToLoad: Boolean
        get() = nextPage <= totalPages

    override fun loadNextPage() {
        scope.launch {
            fetchEpisodes(nextPage)
        }
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

    private suspend fun fetchEpisodes(page: Int) {
        if (!hasMoreEpisodesToLoad) return

        try {
            val response: EpisodesResponse = api.getEpisodes(page)
            db.transaction {
                response.results.forEach { episode ->
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
            nextPage = page + 1
            totalPages = response.info.pages
        } catch (e: Exception) {
            // TODO log
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