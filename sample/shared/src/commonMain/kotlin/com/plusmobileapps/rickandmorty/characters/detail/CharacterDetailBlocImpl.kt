package com.plusmobileapps.rickandmorty.characters.detail

import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.characters.CharactersRepository
import com.plusmobileapps.rickandmorty.util.Consumer
import com.plusmobileapps.rickandmorty.util.asValue

internal class CharacterDetailBlocImpl(
    characterId: Int,
    context: AppComponentContext,
    repository: CharactersRepository,
    private val output: Consumer<CharacterDetailBloc.Output>,
) : CharacterDetailBloc,
    AppComponentContext by context {

    private val store: CharacterDetailStore = instanceKeeper.getStore {
        CharacterDetailStoreProvider(characterId, repository, dispatchers, storeFactory).provide()
    }

    override val models: Value<CharacterDetailBloc.Model> = store.asValue().map {
        CharacterDetailBloc.Model(
            isLoading = it.isLoading,
            character = it.character,
            error = it.error,
        )
    }

    override fun onBackClicked() {
        output(CharacterDetailBloc.Output.Done)
    }
}