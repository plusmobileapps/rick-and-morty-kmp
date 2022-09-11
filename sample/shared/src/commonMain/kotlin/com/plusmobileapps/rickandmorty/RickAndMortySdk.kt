package com.plusmobileapps.rickandmorty

import com.plusmobileapps.rickandmorty.api.RickAndMortyApi
import com.plusmobileapps.rickandmorty.characters.CharactersRepository
import com.plusmobileapps.rickandmorty.characters.CharactersRepositoryImpl
import com.plusmobileapps.rickandmorty.db.MyDatabase
import com.plusmobileapps.rickandmorty.episodes.EpisodesStore
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers

interface RickAndMortySdk {
    val charactersRepository: CharactersRepository
    val episodesStore: EpisodesStore
}

internal class RickAndMortySdkImpl(
    private val database: MyDatabase,
) : RickAndMortySdk {
    private val settings = Settings()

    override val charactersRepository: CharactersRepository = CharactersRepositoryImpl(
        ioContext = Dispatchers.Default,
        db = database.characterQueries,
        settings = settings,
        api = RickAndMortyApi.instance
    )

    override val episodesStore: EpisodesStore
        get() = TODO("Not yet implemented")
}