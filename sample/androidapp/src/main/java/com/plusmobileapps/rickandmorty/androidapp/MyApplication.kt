package com.plusmobileapps.rickandmorty.androidapp

import android.app.Application
import com.plusmobileapps.rickandmorty.RickAndMorty

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        RickAndMorty.init(this)
    }
}