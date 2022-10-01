package com.plusmobileapps.rickandmorty.androidapp.screens.locations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode
import com.plusmobileapps.rickandmorty.androidapp.screens.episodes.CharacterGridItem
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.locations.detail.LocationDetailBloc

@Composable
fun LocationDetailScreen(bloc: LocationDetailBloc, windowSize: WindowSizeClass) {
    val model = bloc.models.subscribeAsState()
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    model.value.location.name,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = bloc::onBackClicked) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
                }
            }
        )
    }) {
        LocationDetailContent(
            windowSize,
            modifier = Modifier.padding(it),
            state = model.value,
            onCharacterClicked = bloc::onCharacterClicked
        )
    }
}

@Composable
private fun LocationDetailContent(
    windowSize: WindowSizeClass,
    modifier: Modifier,
    state: LocationDetailBloc.Model,
    onCharacterClicked: (RickAndMortyCharacter) -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "Name: ${state.location.name}", style = MaterialTheme.typography.displaySmall)
        Text(
            text = "Dimension: ${state.location.dimension}",
            style = MaterialTheme.typography.titleLarge
        )
        AnimatedVisibility(visible = state.isLoadingCharacters) {
            CircularProgressIndicator()
        }
        Divider()
        Text("Characters:", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(modifier = Modifier.fillMaxWidth(), mainAxisSize = SizeMode.Wrap) {
            state.characters.forEach {
                CharacterGridItem(windowSize, character = it) {
                    onCharacterClicked(it)
                }
            }
        }
    }
}