package com.plusmobileapps.rickandmorty.episodes.search

import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.api.RickAndMortyApiClient
import com.plusmobileapps.rickandmorty.episodes.search.EpisodeSearchBloc.Output
import com.plusmobileapps.rickandmorty.util.Consumer
import com.plusmobileapps.rickandmorty.util.asValue

class EpisodeSearchBlocImpl(
    context: AppComponentContext,
    api: RickAndMortyApiClient,
    private val output: Consumer<Output>
) : EpisodeSearchBloc, AppComponentContext by context {

    private val store: EpisodeSearchStore = instanceKeeper.getStore {
        EpisodeSearchStoreProvider(dispatchers, storeFactory, api = api).provide()
    }
    override val models: Value<EpisodeSearchBloc.Model> = store.asValue().map {
        EpisodeSearchBloc.Model(
            isLoading = it.isLoading,
            nameQuery = it.nameQuery,
            results = it.results,
            episodeCode = it.episodeCode,
            error = it.error,
            showFilters = it.showFilters
        )
    }

    override fun onSearchClicked() {
        store.accept(EpisodeSearchStore.Intent.InitiateSearch)
    }

    override fun onClearNameQueryClicked() {
        store.accept(EpisodeSearchStore.Intent.UpdateNameQuery(""))
    }

    override fun onNameQueryChanged(name: String) {
        store.accept(EpisodeSearchStore.Intent.UpdateNameQuery(name))
    }

    override fun onEpisodeCodeChanged(code: String) {
        store.accept(EpisodeSearchStore.Intent.UpdateEpisodeCode(code))
    }

    override fun onClearEpisodeCodeClicked() {
        store.accept(EpisodeSearchStore.Intent.UpdateEpisodeCode(""))
    }

    override fun onBackClicked() {
        output(Output.GoBack)
    }

    override fun onFiltersToggleClicked() {
        store.accept(EpisodeSearchStore.Intent.ToggleFilters)
    }
}