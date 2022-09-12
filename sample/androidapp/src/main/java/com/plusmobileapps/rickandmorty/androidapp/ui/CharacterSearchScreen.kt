package com.plusmobileapps.rickandmorty.androidapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBloc

@Composable
fun CharacterSearchScreen(bloc: CharacterSearchBloc) {
    Scaffold(
        topBar = { LargeTopAppBar(title = { Text(text = "") }) }
    ) {
        val model = bloc.models.subscribeAsState()
        Column(modifier = Modifier.padding(it)) {
            OutlinedTextField(value = model.value.query, onValueChange = bloc::onQueryChanged)
        }
    }
}