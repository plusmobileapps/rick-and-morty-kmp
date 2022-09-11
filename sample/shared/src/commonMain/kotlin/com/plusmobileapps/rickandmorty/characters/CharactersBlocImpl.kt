package com.plusmobileapps.rickandmorty.characters

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.plusmobileapps.rickandmorty.di.DI
import com.plusmobileapps.rickandmorty.util.Dispatchers
import com.plusmobileapps.rickandmorty.util.asValue

class CharactersBlocImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    dispatchers: Dispatchers,
    repository: CharactersRepository,
    private val output: (CharactersBloc.Output) -> Unit
) : CharactersBloc, ComponentContext by componentContext {

    constructor(
        componentContext: ComponentContext,
        di: DI,
        output: (CharactersBloc.Output) -> Unit
    ) : this(
        componentContext = componentContext,
        storeFactory = di.storeFactory,
        dispatchers = di.dispatchers,
        repository = di.charactersRepository,
        output = output
    )

    private val store = instanceKeeper.getStore {
        CharactersStoreProvider(storeFactory, dispatchers, repository).provide()
    }

    override val models: Value<CharactersBloc.Model> = store.asValue().map { state ->
        CharactersBloc.Model(
            characters = state.characters,
            error = state.error,
            isLoading = state.isLoading
        )
    }

    override fun onCharacterClicked(character: RickAndMortyCharacter) {
        output(CharactersBloc.Output.OpenCharacter(character))
    }
}