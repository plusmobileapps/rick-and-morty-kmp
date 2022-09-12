package com.plusmobileapps.rickandmorty.characters.search

import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.api.characters.CharacterGender
import com.plusmobileapps.rickandmorty.api.characters.CharacterStatus
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBloc.Output
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchStore.Intent
import com.plusmobileapps.rickandmorty.di.DI
import com.plusmobileapps.rickandmorty.util.Consumer
import com.plusmobileapps.rickandmorty.util.asValue

internal class CharacterSearchBlocImpl(
    componentContext: AppComponentContext,
    private val output: Consumer<Output>
) : CharacterSearchBloc, AppComponentContext by componentContext {

    constructor(componentContext: AppComponentContext, di: DI, output: Consumer<Output>) : this(
        componentContext = componentContext,
        output = output,
    )

    private val store = instanceKeeper.getStore {
        CharacterSearchStoreProvider(storeFactory, dispatchers).provide()
    }

    override val models: Value<CharacterSearchBloc.Model> = store.asValue().map {
        CharacterSearchBloc.Model(
            isLoading = it.isLoading,
            query = it.query,
            results = it.results,
            status = it.status,
            species = it.species,
            gender = it.gender,
            error = it.error
        )
    }

    override fun onSearchClicked() {
        store.accept(Intent.InitiateSearch)
    }

    override fun onQueryChanged(query: String) {
        store.accept(Intent.UpdateQuery(query))
    }

    override fun onClearQueryClicked() {
        store.accept(Intent.UpdateQuery(""))
    }

    override fun onStatusChanged(status: CharacterStatus) {
        store.accept(Intent.UpdateStatus(status))
    }

    override fun onClearStatusClicked() {
        store.accept(Intent.UpdateStatus(null))
    }

    override fun onSpeciesChanged(species: String) {
        store.accept(Intent.UpdateSpecies(species))
    }

    override fun onClearSpeciesClicked() {
        store.accept(Intent.UpdateSpecies(("")))
    }

    override fun onGenderChanged(gender: CharacterGender) {
        store.accept(Intent.UpdateGender(gender))
    }

    override fun onClearGenderClicked() {
        store.accept(Intent.UpdateGender(null))
    }
}