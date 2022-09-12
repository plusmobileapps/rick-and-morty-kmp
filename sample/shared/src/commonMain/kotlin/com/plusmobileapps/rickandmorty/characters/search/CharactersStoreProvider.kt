package com.plusmobileapps.rickandmorty.characters.search

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.plusmobileapps.rickandmorty.api.characters.CharacterGender
import com.plusmobileapps.rickandmorty.api.characters.CharacterStatus
import com.plusmobileapps.rickandmorty.characters.CharactersListItem
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchStore.Intent
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchStore.State
import com.plusmobileapps.rickandmorty.util.Dispatchers

internal class CharacterSearchStoreProvider(
    private val storeFactory: StoreFactory,
    private val dispatchers: Dispatchers,
) {

    sealed class Message {
        data class UpdateQuery(val query: String) : Message()
        data class ResultsUpdated(val results: List<CharactersListItem>) : Message()
        data class StatusUpdated(val status: CharacterStatus?) : Message()
        data class SpeciesUpdated(val species: String) : Message()
        data class GenderUpdated(val gender: CharacterGender?) : Message()
        object LoadingQuery : Message()
    }

    fun provide(): CharacterSearchStore =
        object : CharacterSearchStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "Store",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::Executor,
            reducer = ReducerImpl
        ) {}

    private inner class Executor :
        CoroutineExecutor<Intent, Unit, State, Message, Nothing>(dispatchers.main) {

    }

    private object ReducerImpl : Reducer<State, Message> {
        override fun State.reduce(msg: Message): State = when (msg) {
            is Message.GenderUpdated -> copy(gender = msg.gender)
            is Message.ResultsUpdated -> copy(results = msg.results, isLoading = false)
            is Message.SpeciesUpdated -> copy(species = msg.species)
            is Message.StatusUpdated -> copy(status = msg.status)
            is Message.UpdateQuery -> copy(query = msg.query)
            Message.LoadingQuery -> copy(isLoading = true)
        }
    }
}