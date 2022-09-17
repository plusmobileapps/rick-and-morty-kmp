package com.plusmobileapps.rickandmorty.episodes.list

import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.plusmobileapps.rickandmorty.AppComponentContext
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.episodes.EpisodesRepository
import com.plusmobileapps.rickandmorty.util.Consumer
import com.plusmobileapps.rickandmorty.util.asValue

internal class EpisodesBlocImpl(
    appComponentContext: AppComponentContext,
    repository: EpisodesRepository,
    private val output: Consumer<EpisodesBloc.Output>,
) : EpisodesBloc, AppComponentContext by appComponentContext {

    private val store: EpisodesStore = instanceKeeper.getStore {
        EpisodesStoreProvider(storeFactory, dispatchers, repository).provide()
    }

    override val models: Value<EpisodesBloc.Model> = store.asValue().map {
        EpisodesBloc.Model(
            isLoading = it.isLoading,
            episodes = it.episodes,
            error = it.error
        )
    }

    override fun onEpisodeClicked(episode: Episode) {
        output(EpisodesBloc.Output.OpenEpisode(episode))
    }

    override fun loadMore() {
        store.accept(EpisodesStore.Intent.LoadMoreCharacters)
    }

    override fun onSearchClicked() {
        output(EpisodesBloc.Output.OpenEpisodeSearch)
    }
}