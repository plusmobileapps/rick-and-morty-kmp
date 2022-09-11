package com.plusmobileapps.rickandmorty

import android.content.Context
import com.plusmobileapps.rickandmorty.db.DriverFactory
import com.plusmobileapps.rickandmorty.db.createDatabase

object RickAndMorty {

    private var _instance: RickAndMortySdk? = null
    val instance: RickAndMortySdk
        get() = _instance
            ?: throw IllegalStateException("Did you call RickAndMortyServiceLocator.init()")

    fun init(context: Context) {
        _instance = RickAndMortySdkImpl(createDatabase(DriverFactory(context)))
    }

}