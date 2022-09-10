package com.plusmobileapps.rickandmorty.api

import com.plusmobileapps.rickandmorty.api.characters.CharacterDTO
import com.plusmobileapps.rickandmorty.api.characters.CharacterGender
import com.plusmobileapps.rickandmorty.api.characters.CharacterStatus
import com.plusmobileapps.rickandmorty.api.characters.CharactersResponse
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.api.episodes.EpisodesResponse
import com.plusmobileapps.rickandmorty.api.locations.Location
import com.plusmobileapps.rickandmorty.api.locations.LocationsResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

/**
 * Http Client for the Rick and Morty API
 * @see [https://rickandmortyapi.com/documentation](https://rickandmortyapi.com/documentation)
 */
interface RickAndMortyApiClient {

    suspend fun getCharacters(
        page: Int,
        name: String? = null,
        status: CharacterStatus? = null,
        species: String? = null,
        type: String? = null,
        gender: CharacterGender? = null,
    ): CharactersResponse

    suspend fun getCharacter(id: Int): CharacterDTO

    suspend fun getCharacters(ids: List<Int>): List<CharacterDTO>

    suspend fun getEpisodes(page: Int, name: String? = null, episodeCode: String? = null): EpisodesResponse

    suspend fun getEpisode(id: Int): Episode

    suspend fun getEpisodes(ids: List<Int>): List<Episode>

    suspend fun getLocations(
        page: Int,
        name: String? = null,
        type: String? = null,
        dimension: String? = null
    ): LocationsResponse

    suspend fun getLocation(id: Int): Location

    suspend fun getLocations(ids: List<Int>): List<Location>
}

object RickAndMortyApi {
    val instance: RickAndMortyApiClient by lazy {
        RickAndMortyApiClientImpl(
            engine = createHttpClientEngine(),
            ioContext = Dispatchers.Default
        )
    }
}

internal class RickAndMortyApiClientImpl(
    engine: HttpClientEngine,
    private val ioContext: CoroutineContext
) : RickAndMortyApiClient {

    private val httpClient = HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json {
                isLenient = true
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        defaultRequest {
            url("https://rickandmortyapi.com/api/")
        }
    }

    override suspend fun getCharacters(
        page: Int,
        name: String?,
        status: CharacterStatus?,
        species: String?,
        type: String?,
        gender: CharacterGender?
    ): CharactersResponse = withContext(ioContext) {
        httpClient.get(CHARACTERS_API) {
            url {
                parameters.append("page", page.toString())
                name?.let { parameters.append("name", it) }
                status?.let { parameters.append(CharacterStatus.QUERY_PARAM, it.apiName) }
                species?.let { parameters.append("species", it) }
                type?.let { parameters.append("type", it) }
                gender?.let { parameters.append(CharacterGender.QUERY_PARAM, it.apiName) }
            }
        }.body()
    }

    override suspend fun getCharacter(id: Int): CharacterDTO = withContext(ioContext) {
        httpClient.get("$CHARACTERS_API/$id").body()
    }

    override suspend fun getCharacters(ids: List<Int>): List<CharacterDTO> =
        withContext(ioContext) {
            val formattedIds = ids.joinToString(separator = ",") { it.toString() }
            httpClient.get("$CHARACTERS_API/$formattedIds").body()
        }

    override suspend fun getEpisodes(
        page: Int,
        name: String?,
        episodeCode: String?
    ): EpisodesResponse =
        withContext(ioContext) {
            httpClient.get(EPISODES_API) {
                url {
                    parameters.append("page", page.toString())
                    name?.let { parameters.append("name", it) }
                    episodeCode?.let { parameters.append("episode", it) }
                }
            }.body()
        }

    override suspend fun getEpisode(id: Int): Episode = withContext(ioContext) {
        httpClient.get("$EPISODES_API/$id").body()
    }

    override suspend fun getEpisodes(ids: List<Int>): List<Episode> = withContext(ioContext) {
        val formattedIds = ids.joinToString(separator = ",") { it.toString() }
        httpClient.get("$EPISODES_API/$formattedIds").body()
    }

    override suspend fun getLocations(
        page: Int,
        name: String?,
        type: String?,
        dimension: String?
    ): LocationsResponse = withContext(ioContext) {
        httpClient.get(LOCATION_API) {
            url {
                parameters.append("page", page.toString())
                name?.let { parameters.append("name", it) }
                type?.let { parameters.append("type", it) }
                dimension?.let { parameters.append("dimension", it) }
            }
        }.body()
    }

    override suspend fun getLocation(id: Int): Location = withContext(ioContext) {
        httpClient.get("$LOCATION_API/$id").body()
    }

    override suspend fun getLocations(ids: List<Int>): List<Location> = withContext(ioContext) {
        val formattedIds = ids.joinToString(separator = ",") { it.toString() }
        httpClient.get("$LOCATION_API/$formattedIds").body()
    }

    companion object {
        const val CHARACTERS_API = "character"
        const val EPISODES_API = "episode"
        const val LOCATION_API = "location"
    }
}