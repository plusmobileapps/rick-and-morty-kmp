package com.plusmobileapps.rickandmorty.characters.search

import com.plusmobileapps.paging.PageLoader
import com.plusmobileapps.paging.PageLoaderRequest
import com.plusmobileapps.paging.PageLoaderResponse
import com.plusmobileapps.paging.PagingDataSource
import com.plusmobileapps.rickandmorty.api.RickAndMortyApiClient
import com.plusmobileapps.rickandmorty.api.characters.CharacterGender
import com.plusmobileapps.rickandmorty.api.characters.CharacterStatus
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchUseCaseImpl.Input
import kotlinx.coroutines.flow.StateFlow

interface CharacterSearchUseCase {
    val pageLoaderState: StateFlow<PagingDataSource.State<RickAndMortyCharacter>>
    fun loadFirstPage(
        query: String,
        status: CharacterStatus?,
        species: String,
        gender: CharacterGender?,
    )

    fun loadNextPage()
}

internal class CharacterSearchUseCaseImpl(
    private val api: RickAndMortyApiClient,
    private val factory: PagingDataSource.Factory,
) : CharacterSearchUseCase, PageLoader<Input, RickAndMortyCharacter> {

    private val pageDataSource = factory.create<Input, RickAndMortyCharacter>(
        pageLoader = this,
    )

    override val pageLoaderState: StateFlow<PagingDataSource.State<RickAndMortyCharacter>> =
        pageDataSource.state

    override fun loadFirstPage(
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

    override fun loadNextPage() {
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