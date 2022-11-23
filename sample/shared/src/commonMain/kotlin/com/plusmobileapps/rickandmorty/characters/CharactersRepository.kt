package com.plusmobileapps.rickandmorty.characters

import com.plusmobileapps.paging.PageLoaderResponse
import com.plusmobileapps.paging.PageLoaderState
import com.plusmobileapps.paging.PagingDataSource
import com.plusmobileapps.rickandmorty.api.RickAndMortyApiClient
import com.plusmobileapps.rickandmorty.api.characters.CharactersResponse
import com.plusmobileapps.rickandmorty.db.CharacterQueries
import com.plusmobileapps.rickandmorty.util.UuidUtil
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.squareup.sqldelight.TransactionWithoutReturn
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
    private val uuidUtil: UuidUtil,
    pagingDataSourceFactory: PagingDataSource.Factory,
) : CharactersRepository {

    companion object {
        const val CHARACTERS_PAGE_KEY = "CHARACTERS_PAGE_KEY"
        const val TOTAL_PAGES_KEY = "CHARACTER_TOTAL_PAGES_KEY"
        const val PAGING_URL = "https://rickandmortyapi.com/api/character?page="
        const val PAGE_SIZE = 20
    }

    private var nextPage = settings.getInt(CHARACTERS_PAGE_KEY, 1)

    private val pagingDataSource: PagingDataSource<Unit, RickAndMortyCharacter> =
        pagingDataSourceFactory.create { request ->
            try {
                val response = api.getCharacters(
                    page = request.pagingKey?.toIntOrNull() ?: 1,
                )
                PageLoaderResponse.Success(
                    data = response.results.map { RickAndMortyCharacter.fromDTO(it) }.also {
                        db.transaction {
                            if (request.pagingKey == null) {
                                db.deleteAll()
                            }
                            insertCharactersIntoDb(it)
                        }
                    },
                    pagingToken = response.info.next?.removePrefix(PAGING_URL)?.toIntOrNull()
                        ?.toString()
                )
            } catch (e: Exception) {
                PageLoaderResponse.Error(
                    canRetrySameRequest = true,
                    message = e.message.toString()
                )
            }

        }

    private val job = Job()
    private val scope = CoroutineScope(ioContext + job)

    init {
        pagingDataSource.loadFirstPage(
            input = Unit,
            pageSize = PAGE_SIZE,
            requestKey = uuidUtil.randomUuid()
        )
    }

    override val hasMoreCharactersToLoad: Boolean
        get() = pagingDataSource.pageLoaderData.value.pageLoaderState.let {
            it is PageLoaderState.Loading
                    || (it as? PageLoaderState.Idle)?.hasMorePages == true
                    || (it as? PageLoaderState.Failed)?.canRetrySameRequest == true
        }

    override fun loadNextPage() {
        pagingDataSource.loadNextPage(
            input = Unit,
            requestKey = uuidUtil.randomUuid()
        )
//        scope.launch { fetchCharacters(page = nextPage) }
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
            if (ids.size == 1) {
                listOf(RickAndMortyCharacter.fromDTO(api.getCharacter(ids.first())))
            } else {
                api.getCharacters(ids).map { RickAndMortyCharacter.fromDTO(it) }
            }.also {
                db.transaction {
                    insertCharactersIntoDb(it)
                }
            }
        }

    private suspend fun fetchCharacters(page: Int) {
        if (!hasMoreCharactersToLoad) return
        try {
            val response: CharactersResponse = api.getCharacters(page)
            val characters = response.results.map { RickAndMortyCharacter.fromDTO(it) }
            db.transaction {
                insertCharactersIntoDb(characters)
            }
            nextPage = page + 1
//            totalPages = response.info.pages
            settings[CHARACTERS_PAGE_KEY] = page + 1
            settings[TOTAL_PAGES_KEY] = response.info.pages
        } catch (e: Exception) {
            // Todo log
        }
    }


    private fun TransactionWithoutReturn.insertCharactersIntoDb(characters: List<RickAndMortyCharacter>) {
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