package com.plusmobileapps.rickandmorty.di

import com.plusmobileapps.rickandmorty.api.RickAndMortyApi
import com.plusmobileapps.rickandmorty.api.RickAndMortyApiClient

interface DI {
    val rickAndMortyApi: RickAndMortyApiClient
}

object ServiceLocator : DI {
    override val rickAndMortyApi: RickAndMortyApiClient = RickAndMortyApi.instance
}