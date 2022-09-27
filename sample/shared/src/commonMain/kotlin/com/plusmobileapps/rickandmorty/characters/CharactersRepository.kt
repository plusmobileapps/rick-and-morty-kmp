package com.plusmobileapps.rickandmorty.characters

import com.plusmobileapps.rickandmorty.api.RickAndMortyApiClient
import com.plusmobileapps.rickandmorty.api.characters.CharactersResponse
import com.plusmobileapps.rickandmorty.db.CharacterQueries
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

interface CharactersRepository {
    val hasMoreCharactersToLoad: Boolean
    fun loadNextPage()
    suspend fun getCharacters(): Flow<List<RickAndMortyCharacter>>
    suspend fun getCharacter(id: Int): RickAndMortyCharacter
    suspend fun getCharacters(ids: List<Int>): List<RickAndMortyCharacter>
}

internal class CharactersRepositoryImpl(
    private val ioContext: CoroutineContext,
    private val db: CharacterQueries,
    private val settings: Settings,
    private val api: RickAndMortyApiClient,
) : CharactersRepository {

    companion object {
        const val CHARACTERS_PAGE_KEY = "CHARACTERS_PAGE_KEY"
        const val TOTAL_PAGES_KEY = "CHARACTER_TOTAL_PAGES_KEY"
    }

    private var nextPage = settings.getInt(CHARACTERS_PAGE_KEY, 1)
    private var totalPages = settings.getInt(TOTAL_PAGES_KEY, Int.MAX_VALUE)

    private val job = Job()
    private val scope = CoroutineScope(ioContext + job)

    init {
        if (nextPage == 1) loadNextPage()
    }

    override val hasMoreCharactersToLoad: Boolean
        get() = nextPage < totalPages

    override fun loadNextPage() {
        scope.launch { fetchCharacters(page = nextPage) }
    }

    override suspend fun getCharacters(): Flow<List<RickAndMortyCharacter>> =
        db.selectAll()
            .asFlow()
            .mapToList()
            .map { characters ->
                characters.map { character ->
                    RickAndMortyCharacter(
                        id = character.id.toInt(),
                        name = character.name,
                        imageUrl = character.imageUrl,
                        status = character.status,
                        species = character.species
                    )
                }
            }

    override suspend fun getCharacter(id: Int): RickAndMortyCharacter =
        withContext(ioContext) {
            val character = db.getCharacter(id.toLong()).executeAsOne()
            RickAndMortyCharacter(
                id = character.id.toInt(),
                name = character.name,
                imageUrl = character.imageUrl,
                status = character.status,
                species = character.species
            )
        }

    override suspend fun getCharacters(ids: List<Int>): List<RickAndMortyCharacter> =
        withContext(ioContext) {
            api.getCharacters(ids).map { RickAndMortyCharacter.fromDTO(it) }
                .also { insertCharactersIntoDb(it) }
        }

    private suspend fun fetchCharacters(page: Int) {
        if (!hasMoreCharactersToLoad) return
        try {
            val response: CharactersResponse = api.getCharacters(page)
            val characters = response.results.map { RickAndMortyCharacter.fromDTO(it) }
            insertCharactersIntoDb(characters)
            nextPage = page + 1
            totalPages = response.info.pages
            settings[CHARACTERS_PAGE_KEY] = page + 1
            settings[TOTAL_PAGES_KEY] = response.info.pages
        } catch (e: Exception) {
            // Todo log
        }
    }

    private fun insertCharactersIntoDb(characters: List<RickAndMortyCharacter>) {
        db.transaction {
            characters.forEach { character ->
                db.insertCharacter(
                    id = character.id.toLong(),
                    name = character.name,
                    imageUrl = character.imageUrl,
                    status = character.status,
                    species = character.species
                )
            }
        }
    }
}