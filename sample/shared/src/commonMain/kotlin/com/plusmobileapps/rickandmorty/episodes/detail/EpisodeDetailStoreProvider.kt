package com.plusmobileapps.rickandmorty.episodes.detail

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.episodes.EpisodesRepository
import com.plusmobileapps.rickandmorty.episodes.detail.EpisodeDetailStore.State
import com.plusmobileapps.rickandmorty.util.Dispatchers
import kotlinx.coroutines.launch

internal class EpisodeDetailStoreProvider(
    private val id: Int,
    private val dispatchers: Dispatchers,
    private val storeFactory: StoreFactory,
    private val repository: EpisodesRepository,
) {

    sealed interface Message {
        data class EpisodeUpdated(val episode: Episode) : Message
        data class CharactersUpdated(val characters: List<RickAndMortyCharacter>) : Message
    }

    fun provide(): EpisodeDetailStore =
        object : EpisodeDetailStore, Store<Nothing, State, Nothing> by storeFactory.create(
            name = "EpisodeDetailStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private inner class ExecutorImpl :
        CoroutineExecutor<Nothing, Unit, State, Message, Nothing>(dispatchers.main) {
        override fun executeAction(action: Unit, getState: () -> State) {
            scope.launch {
                val episode = repository.getEpisode(id)
                dispatch(Message.EpisodeUpdated(episode))
            }
        }
    }

    private object ReducerImpl : Reducer<State, Message> {
        override fun State.reduce(msg: Message): State = when (msg) {
            is Message.CharactersUpdated -> copy(characters = msg.characters)
            is Message.EpisodeUpdated -> copy(isLoading = false, episode = msg.episode)
        }
    }

}