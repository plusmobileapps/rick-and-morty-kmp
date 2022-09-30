package com.plusmobileapps.rickandmorty.androidapp.screens.episodes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.episodes.detail.EpisodeDetailBloc

@Composable
fun EpisodeDetailScreen(bloc: EpisodeDetailBloc, windowSize: WindowSizeClass) {
    val model = bloc.models.subscribeAsState()
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    "Episode ${model.value.episode.episode}",
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
        EpisodeDetailContent(
            windowSize,
            modifier = Modifier.padding(it),
            state = model.value,
            onCharacterClicked = bloc::onCharacterClicked
        )
    }
}

@Composable
private fun EpisodeDetailContent(
    windowSize: WindowSizeClass,
    modifier: Modifier,
    state: EpisodeDetailBloc.Model,
    onCharacterClicked: (Int) -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = state.episode.name, style = MaterialTheme.typography.displaySmall)
        Text(
            text = "Aired on ${state.episode.air_date}",
            style = MaterialTheme.typography.titleLarge
        )
        Divider()
        AnimatedVisibility(visible = state.isLoadingCharacters) {
            CircularProgressIndicator()
        }
        Text("Characters:", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(modifier = Modifier.fillMaxWidth(), mainAxisSize = SizeMode.Wrap) {
            state.characters.forEach {
                CharacterGridItem(windowSize, character = it) {
                    onCharacterClicked(it.id)
                }
            }
        }
    }
}

@Composable
fun CharacterGridItem(
    windowSize: WindowSizeClass,
    character: RickAndMortyCharacter,
    onClick: () -> Unit
) {
    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidthDp.dp
    val numOfCardsInRow = when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 2
        WindowWidthSizeClass.Medium -> 3
        else -> 4
    }
    val cardPadding = 8.dp
    val size = (screenWidth / numOfCardsInRow) - (cardPadding * 2)

    Card(
        modifier = Modifier
            .padding(cardPadding)
            .width(size)
            .clickable { onClick() }
            .semantics {
                contentDescription = character.name
            },
    ) {
        Column {
            AsyncImage(
                modifier = Modifier.size(size),
                model = character.imageUrl,
                contentDescription = null
            )
            Text(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
                text = character.name,
                style = MaterialTheme.typography.titleLarge,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}