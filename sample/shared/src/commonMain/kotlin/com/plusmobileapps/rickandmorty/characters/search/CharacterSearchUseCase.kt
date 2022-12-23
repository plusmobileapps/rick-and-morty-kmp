package com.plusmobileapps.rickandmorty.characters.search

import com.plusmobileapps.paging.*
import com.plusmobileapps.rickandmorty.api.RickAndMortyApiClient
import com.plusmobileapps.rickandmorty.api.characters.CharacterGender
import com.plusmobileapps.rickandmorty.api.characters.CharacterStatus
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchUseCaseImpl.Input
import kotlinx.coroutines.flow.Flow

interface CharacterSearchUseCase {
    val pageLoaderState: Flow<PagingDataSourceState<RickAndMortyCharacter>>
    suspend fun loadFirstPage(
        query: String,
        status: CharacterStatus?,
        species: String,
        gender: CharacterGender?,
    )

    suspend fun loadNextPage()
}

internal class CharacterSearchUseCaseImpl(
    private val api: RickAndMortyApiClient,
    factory: InMemoryPageLoader.Factory,
) : CharacterSearchUseCase, PageLoader<Input, RickAndMortyCharacter> {

    private val pageDataSource: InMemoryPageLoader<Input, RickAndMortyCharacter> =
        factory.create(this)

    override val pageLoaderState: Flow<PagingDataSourceState<RickAndMortyCharacter>> =
        pageDataSource.state

    override suspend fun loadFirstPage(
        query: String,
        status: CharacterStatus?,
        species: String,
        gender: CharacterGender?
    ) {
        pageDataSource.clearAndLoadFirstPage(
            input = Input(
                query = query,
                status = status,
                species = species,
                gender = gender,
            )
        )
    }

    override suspend fun loadNextPage() {
        pageDataSource.loadNextPage()
    }

    override suspend fun load(request: PageLoaderRequest<Input>): PageLoaderResponse<RickAndMortyCharacter> {
        return try {
            val response = api.getCharacters(
                page = request.pagingKey?.toIntOrNull() ?: 0,
                name = request.input.query,
                status = request.input.status,
                species = request.input.species,
                type = null, // TODO
                gender = request.input.gender
            )
            val characters = response.results.map { RickAndMortyCharacter.fromDTO(it) }
            PageLoaderResponse.Success(
                data = characters,
                pagingToken = response.info.nextPageNumber
            )
        } catch (e: Exception) {
            PageLoaderResponse.Error(exception = e)
        }
    }

    internal data class Input(
        val query: String = "",
        val status: CharacterStatus? = null,
        val species: String = "",
        val gender: CharacterGender? = null,
    )

}