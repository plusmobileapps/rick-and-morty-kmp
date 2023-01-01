package com.plusmobileapps.rickandmorty.episodes.search

import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.api.RickAndMortyApiClient
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.episodes.search.EpisodeSearchBloc.Output
import com.plusmobileapps.rickandmorty.episodes.search.EpisodeSearchStore.Intent
import com.plusmobileapps.rickandmorty.util.Consumer
import com.plusmobileapps.rickandmorty.util.asValue

class EpisodeSearchBlocImpl(
    context: AppComponentContext,
    useCase: EpisodeSearchUseCase,
    private val output: Consumer<Output>
) : EpisodeSearchBloc, AppComponentContext by context {

    private val store: EpisodeSearchStore = instanceKeeper.getStore {
        EpisodeSearchStoreProvider(dispatchers, storeFactory, useCase).provide()
    }
    override val models: Value<EpisodeSearchBloc.Model> = store.asValue().map {
        EpisodeSearchBloc.Model(
            pageLoaderState = it.pageLoaderState,
            nameQuery = it.nameQuery,
            episodeCode = it.episodeCode,
            error = it.error,
            showFilters = it.showFilters
        )
    }

    override fun onSearchClicked() {
        store.accept(Intent.InitiateSearch)
    }

    override fun loadMoreResults() {
        store.accept(Intent.LoadMoreResults)
    }

    override fun onFirstPageTryAgainClicked() {
        store.accept(Intent.InitiateSearch)
    }

    override fun onNextPageTryAgainClicked() {
        store.accept(Intent.LoadMoreResults)
    }

    override fun onClearNameQueryClicked() {
        store.accept(Intent.UpdateNameQuery(""))
    }

    override fun onNameQueryChanged(name: String) {
        store.accept(Intent.UpdateNameQuery(name))
    }

    override fun onEpisodeCodeChanged(code: String) {
        store.accept(Intent.UpdateEpisodeCode(code))
    }

    override fun onClearEpisodeCodeClicked() {
        store.accept(Intent.UpdateEpisodeCode(""))
    }

    override fun onBackClicked() {
        output(Output.GoBack)
    }

    override fun onFiltersToggleClicked() {
        store.accept(Intent.ToggleFilters)
    }

    override fun onEpisodeClicked(episode: Episode) {
        output(Output.OpenEpisode(episode.id))
    }
}