package com.plusmobileapps.rickandmorty.androidapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.plusmobileapps.rickandmorty.episodes.detail.EpisodeDetailBloc

@Composable
fun EpisodeDetailScreen(bloc: EpisodeDetailBloc) {
    val model = bloc.models.subscribeAsState()
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Episode Detail") },
            navigationIcon = {
                IconButton(onClick = bloc::onBackClicked) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
                }
            }
        )
    }) {
        EpisodeDetailContent(
            modifier = Modifier.padding(it),
            state = model.value,
            onCharacterClicked = bloc::onCharacterClicked
        )
    }
}

@Composable
private fun EpisodeDetailContent(
    modifier: Modifier,
    state: EpisodeDetailBloc.Model,
    onCharacterClicked: (Int) -> Unit
) {
    Column(modifier = modifier) {
        Text(text = state.episode.name)
        Text(text = state.episode.air_date)
        Text(text = state.episode.created)
        Text(text = state.episode.episode)

        Text(text = "Characters")

    }
}