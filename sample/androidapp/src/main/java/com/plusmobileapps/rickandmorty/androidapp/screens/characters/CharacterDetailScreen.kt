package com.plusmobileapps.rickandmorty.androidapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.plusmobileapps.rickandmorty.characters.detail.CharacterDetailBloc

@Composable
fun CharacterDetailScreen(bloc: CharacterDetailBloc) {
    val model = bloc.models.subscribeAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Character detail") },
                navigationIcon = {
                    IconButton(onClick = bloc::onBackClicked) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) {
        CharacterDetailContent(modifier = Modifier.padding(it), state = model.value)
    }
}

@Composable
fun CharacterDetailContent(modifier: Modifier, state: CharacterDetailBloc.Model) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            modifier = Modifier.size(250.dp),
            model = state.character.imageUrl,
            contentDescription = null
        )
        Text(
            state.character.name,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            state.character.species,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Text(
            state.character.status,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}