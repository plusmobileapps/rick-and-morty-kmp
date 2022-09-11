package com.plusmobileapps.rickandmorty.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.plusmobileapps.rickandmorty.api.RickAndMortyApi
import com.plusmobileapps.rickandmorty.api.RickAndMortyApiClient
import com.plusmobileapps.rickandmorty.characters.CharactersRepository
import com.plusmobileapps.rickandmorty.characters.CharactersRepositoryImpl
import com.plusmobileapps.rickandmorty.db.DriverFactory
import com.plusmobileapps.rickandmorty.db.MyDatabase
import com.plusmobileapps.rickandmorty.db.createDatabase
import com.plusmobileapps.rickandmorty.util.Dispatchers
import com.plusmobileapps.rickandmorty.util.DispatchersImpl
import com.russhwolf.settings.Settings

interface DI {
    val rickAndMortyApi: RickAndMortyApiClient
    val storeFactory: StoreFactory
    val dispatchers: Dispatchers
    val database: MyDatabase
    val settings: Settings
    val charactersRepository: CharactersRepository
}

class ServiceLocator(driverFactory: DriverFactory) : DI {
    override val rickAndMortyApi: RickAndMortyApiClient get() = RickAndMortyApi.instance
    override val storeFactory: StoreFactory = DefaultStoreFactory()
    override val dispatchers: Dispatchers = DispatchersImpl
    override val settings: Settings by lazy { Settings() }
    override val database: MyDatabase = createDatabase(driverFactory)
    override val charactersRepository: CharactersRepository by lazy {
        CharactersRepositoryImpl(
            ioContext = dispatchers.default,
            db = database.characterQueries,
            settings = settings,
            api = rickAndMortyApi
        )
    }
}