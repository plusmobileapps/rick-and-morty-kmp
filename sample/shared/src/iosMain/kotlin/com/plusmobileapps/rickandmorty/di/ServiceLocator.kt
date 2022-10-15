package com.plusmobileapps.rickandmorty.di

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.plusmobileapps.rickandmorty.DefaultAppComponentContext
import com.plusmobileapps.rickandmorty.characters.list.CharactersBloc
import com.plusmobileapps.rickandmorty.characters.list.CharactersBlocImpl
import com.plusmobileapps.rickandmorty.db.DriverFactory

object BlocBuilder {
    val di: DI = ServiceLocator(DriverFactory())

    var charactersBlocOutput: (CharactersBloc.Output) -> Unit = {}

    fun createCharactersList(lifecycle: Lifecycle): CharactersBloc =
        CharactersBlocImpl(
            componentContext = DefaultAppComponentContext(
                componentContext = DefaultComponentContext(lifecycle),
                dispatchers = di.dispatchers,
                storeFactory = di.storeFactory
            ),
            di = di,
            output = { charactersBlocOutput(it) }
        ).also { lifecycle.doOnDestroy { charactersBlocOutput = {} } }

}