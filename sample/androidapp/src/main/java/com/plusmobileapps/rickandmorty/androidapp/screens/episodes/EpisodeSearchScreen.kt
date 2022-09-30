package com.plusmobileapps.rickandmorty.androidapp.screens.episodes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.plusmobileapps.rickandmorty.androidapp.R
import com.plusmobileapps.rickandmorty.androidapp.theme.Rick_and_Morty_KMPTheme
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.episodes.EpisodeListItem
import com.plusmobileapps.rickandmorty.episodes.search.EpisodeSearchBloc

@Composable
fun EpisodeSearchScreen(bloc: EpisodeSearchBloc) {
    val model = bloc.models.subscribeAsState()
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EpisodeSearchBar(
            query = model.value.nameQuery,
            showFilters = model.value.showFilters,
            onBackClicked = bloc::onBackClicked,
            onQueryChanged = bloc::onNameQueryChanged,
            onSearchClicked = bloc::onSearchClicked,
            onFiltersClicked = bloc::onFiltersToggleClicked
        )
        AnimatedVisibility(model.value.showFilters) {
            EpisodeSearchExtraFiltersUI(
                episodeCode = model.value.episodeCode,
                episodeCodeChanged = bloc::onEpisodeCodeChanged,
                onClearEpisodeCodeClicked = bloc::onClearEpisodeCodeClicked
            )
        }
        AnimatedVisibility(model.value.isLoading) {
            CircularProgressIndicator()
        }
        val items = model.value.results
        if (items.isEmpty()) {
            Text(modifier = Modifier.weight(1f), text = "No results")
        } else {
            EpisodeSearchResults(
                modifier = Modifier.weight(1f),
                episodes = items,
                onEpisodeClicked = { })
        }
    }

}

@Composable
fun EpisodeSearchBar(
    query: String,
    showFilters: Boolean,
    onBackClicked: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onSearchClicked: () -> Unit,
    onFiltersClicked: () -> Unit
) {
    SmallTopAppBar(
        modifier = Modifier.shadow(4.dp),
        navigationIcon = {
            IconButton(onBackClicked, modifier = Modifier.padding(4.dp)) {
                Icon(Icons.Default.ArrowBack, "Go Back")
            }
        },
        title = {
            TextField(
                modifier = Modifier.padding(4.dp),
                value = query,
                onValueChange = onQueryChanged,
                label = { Text("Episode name") },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(onSearch = { onSearchClicked() })
            )
        },
        actions = {
            IconButton(onClick = onSearchClicked) {
                Icon(Icons.Default.Search, contentDescription = "Search characters")
            }
            IconButton(onClick = onFiltersClicked) {
                if (showFilters) {
                    Icon(Icons.Default.Close, contentDescription = "Close Filters")
                } else {
                    Icon(
                        painterResource(id = R.drawable.ic_tune),
                        contentDescription = "Open Filters"
                    )
                }
            }
        }
    )
}

@Composable
fun EpisodeSearchResults(
    modifier: Modifier,
    episodes: List<EpisodeListItem>,
    onEpisodeClicked: (Episode) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(episodes) {
            when (it) {
                is EpisodeListItem.EpisodeItem -> EpisodeListItemCard(episode = it.value) {
                    onEpisodeClicked(it.value)
                }
                EpisodeListItem.NextPageLoading -> CircularProgressIndicator()
            }

        }
    }
}

@Composable
fun EpisodeSearchExtraFiltersUI(
    episodeCode: String,
    episodeCodeChanged: (String) -> Unit,
    onClearEpisodeCodeClicked: () -> Unit,
) {
    Column {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            value = episodeCode,
            onValueChange = episodeCodeChanged,
            label = { Text("Episode Code") },
            trailingIcon = {
                IconButton(onClick = { onClearEpisodeCodeClicked() }) {
                    Icon(Icons.Default.Clear, "Clear Species")
                }
            })
    }
}

@Preview
@Composable
fun EpisodeSearchPreview() {
    var showFilters by remember {
        mutableStateOf(false)
    }
    Rick_and_Morty_KMPTheme {
        Surface {

        }
    }
}