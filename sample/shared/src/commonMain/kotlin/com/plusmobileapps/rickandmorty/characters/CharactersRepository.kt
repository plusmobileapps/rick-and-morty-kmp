package com.plusmobileapps.rickandmorty.characters

import com.plusmobileapps.paging.*
import com.plusmobileapps.rickandmorty.api.RickAndMortyApiClient
import com.plusmobileapps.rickandmorty.db.CharacterQueries
import com.squareup.sqldelight.TransactionWithoutReturn
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

interface CharactersRepository {
    val pageLoaderState: StateFlow<PagingDataSource.State<RickAndMortyCharacter>>
    val hasMoreToLoad: Boolean
    fun loadNextPage()
    suspend fun getCharacters(): Flow<List<RickAndMortyCharacter>>
    suspend fun getCharacter(id: Int): RickAndMortyCharacter
    suspend fun getCharacters(ids: List<Int>): List<RickAndMortyCharacter>
}

internal class CharactersRepositoryImpl(
    private val ioContext: CoroutineContext,
    private val db: CharacterQueries,
    private val api: RickAndMortyApiClient,
    pagingDataSourceFactory: PagingDataSource.Factory,
) : CharactersRepository {

    companion object {
        const val PAGE_SIZE = 20
    }

    private val pagingDataSource: PagingDataSource<Unit, RickAndMortyCharacter> =
        pagingDataSourceFactory.create(this::loadPage)

    override val pageLoaderState: StateFlow<PagingDataSource.State<RickAndMortyCharacter>>
        get() = pagingDataSource.state

    init {
        pagingDataSource.clearAndLoadFirstPage(
            input = Unit,
        )
    }

    override val hasMoreToLoad: Boolean
        get() = pagingDataSource.state.value.hasMoreToLoad

    override fun loadNextPage() {
        pagingDataSource.loadNextPage()
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

    private suspend fun loadPage(request: PageLoaderRequest<Unit>): PageLoaderResponse<RickAndMortyCharacter> {
        return try {
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
                pagingToken = response.info.nextPageNumber
            )
        } catch (e: Exception) {
            PageLoaderResponse.Error(
                exception = e,
                message = e.message.toString()
            )
        }
    }
}