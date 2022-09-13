package com.plusmobileapps.rickandmorty.androidapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.plusmobileapps.rickandmorty.characters.CharactersListItem
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBloc

@Composable
fun CharacterSearchScreen(bloc: CharacterSearchBloc) {
    Scaffold(
        topBar = { LargeTopAppBar(title = { Text(text = "") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = bloc::onSearchClicked) {
                Text(text = "Search")
            }
        }
    ) {
        val model = bloc.models.subscribeAsState()
        Column(modifier = Modifier.padding(it)) {
            OutlinedTextField(value = model.value.query, onValueChange = bloc::onQueryChanged)
            val items = model.value.results
            if (items.isEmpty()) {
                Text(text = "No results")
            } else {
                LazyColumn {
                    items(items) {
                        when(it) {
                            is CharactersListItem.Character ->  CharacterListItem(character = it.value) {
                                // TODO character click
                            }
                            is CharactersListItem.PageLoading -> TODO()
                        }
                       
                    }
                }
            }
            
        }
    }
}