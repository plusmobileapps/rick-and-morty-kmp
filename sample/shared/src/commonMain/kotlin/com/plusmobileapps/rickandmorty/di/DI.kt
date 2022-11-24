package com.plusmobileapps.rickandmorty.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.plusmobileapps.konnectivity.Konnectivity
import com.plusmobileapps.paging.PagingDataSourceFactory
import com.plusmobileapps.rickandmorty.api.RickAndMortyApi
import com.plusmobileapps.rickandmorty.api.RickAndMortyApiClient
import com.plusmobileapps.rickandmorty.characters.CharactersRepository
import com.plusmobileapps.rickandmorty.characters.CharactersRepositoryImpl
import com.plusmobileapps.rickandmorty.db.DriverFactory
import com.plusmobileapps.rickandmorty.db.MyDatabase
import com.plusmobileapps.rickandmorty.db.createDatabase
import com.plusmobileapps.rickandmorty.episodes.EpisodesRepository
import com.plusmobileapps.rickandmorty.episodes.EpisodesRepositoryImpl
import com.plusmobileapps.rickandmorty.locations.LocationRepository
import com.plusmobileapps.rickandmorty.locations.LocationRepositoryImpl
import com.plusmobileapps.rickandmorty.util.Dispatchers
import com.plusmobileapps.rickandmorty.util.DispatchersImpl
import com.plusmobileapps.rickandmorty.util.UuidUtil
import com.plusmobileapps.rickandmorty.util.UuidUtilImpl
import com.russhwolf.settings.Settings

interface DI {
    val rickAndMortyApi: RickAndMortyApiClient
    val konnectivity: Konnectivity
    val storeFactory: StoreFactory
    val dispatchers: Dispatchers
    val database: MyDatabase
    val settings: Settings
    val charactersRepository: CharactersRepository
    val episodesRepository: EpisodesRepository
    val locationRepository: LocationRepository
    val uuidUtil: UuidUtil
}

class ServiceLocator(driverFactory: DriverFactory) : DI {
    override val rickAndMortyApi: RickAndMortyApiClient get() = RickAndMortyApi.instance
    override val konnectivity: Konnectivity = Konnectivity()
    override val storeFactory: StoreFactory = DefaultStoreFactory()
    override val dispatchers: Dispatchers = DispatchersImpl
    override val settings: Settings by lazy { Settings() }
    override val database: MyDatabase = createDatabase(driverFactory)
    override val uuidUtil: UuidUtil = UuidUtilImpl()
    override val charactersRepository: CharactersRepository by lazy {
        CharactersRepositoryImpl(
            ioContext = dispatchers.default,
            db = database.characterQueries,
            api = rickAndMortyApi,
            pagingDataSourceFactory = PagingDataSourceFactory,
        )
    }
    override val episodesRepository: EpisodesRepository by lazy {
        EpisodesRepositoryImpl(
            api = rickAndMortyApi,
            dispatchers = dispatchers,
            db = database.episodeQueries,
            settings = settings
        )
    }
    override val locationRepository: LocationRepository by lazy {
        LocationRepositoryImpl(
            api = rickAndMortyApi,
            dispatchers = dispatchers,
            db = database.locationQueries,
            settings = settings
        )
    }
}