package com.plusmobileapps.rickandmorty.characters.list

import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.characters.CharactersRepository
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.di.DI
import com.plusmobileapps.rickandmorty.util.asValue

internal class CharactersBlocImpl(
    componentContext: AppComponentContext,
    repository: CharactersRepository,
    private val output: (CharactersBloc.Output) -> Unit
) : CharactersBloc, AppComponentContext by componentContext {

    constructor(
        componentContext: AppComponentContext,
        di: DI,
        output: (CharactersBloc.Output) -> Unit
    ) : this(
        componentContext = componentContext,
        repository = di.charactersRepository,
        output = output
    )

    private val store: CharactersStore = instanceKeeper.getStore {
        CharactersStoreProvider(storeFactory, dispatchers, repository).provide()
    }

    override val models: Value<CharactersBloc.Model> = store.asValue().map { state ->
        CharactersBloc.Model(
            listItems = state.items,
            error = state.error,
            isLoading = state.isLoading
        )
    }

    override fun loadMoreCharacters() {
        store.accept(CharactersStore.Intent.LoadMoreCharacters)
    }

    override fun onCharacterClicked(character: RickAndMortyCharacter) {
        output(CharactersBloc.Output.OpenCharacter(character))
    }
}