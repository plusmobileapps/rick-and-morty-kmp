package com.plusmobileapps.rickandmorty.androidapp.screens.episodes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.plusmobileapps.rickandmorty.androidapp.util.rememberScrollContext
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.episodes.EpisodeListItem
import com.plusmobileapps.rickandmorty.episodes.list.EpisodesBloc
import kotlinx.coroutines.launch

@Composable
fun EpisodesUI(bloc: EpisodesBloc) {
    val model = bloc.models.subscribeAsState()
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val scrollContext = rememberScrollContext(listState = lazyListState)

    if (scrollContext.isBottom) bloc.loadMore()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Episodes") }, actions = {
                IconButton(onClick = bloc::onSearchClicked) {
                    Icon(Icons.Default.Search, contentDescription = "Search Episodes")
                }
            })
        },
        floatingActionButton = {
            AnimatedVisibility(visible = !scrollContext.isBottom) {
                FloatingActionButton(onClick = {
                    scope.launch {
                        lazyListState.animateScrollToItem(model.value.episodes.lastIndex)
                    }
                }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
            }
        }
    ) {
        EpisodesList(
            modifier = Modifier.padding(it),
            lazyListState = lazyListState,
            episodes = model.value.episodes,
            onEpisodeClicked = bloc::onEpisodeClicked
        )
    }
}

@Composable
fun EpisodesList(
    modifier: Modifier,
    lazyListState: LazyListState,
    episodes: List<EpisodeListItem>,
    onEpisodeClicked: (Episode) -> Unit,
) {
    LazyColumn(modifier = modifier, state = lazyListState) {
        items(episodes, key = {
            when (it) {
                is EpisodeListItem.EpisodeItem -> it.value.id
                is EpisodeListItem.NextPageLoading -> EpisodeListItem.NextPageLoading.KEY
            }
        }) {
            when (it) {
                is EpisodeListItem.EpisodeItem -> EpisodeListItemCard(episode = it.value) {
                    onEpisodeClicked(it.value)
                }
                is EpisodeListItem.NextPageLoading -> Text(
                    "Loading another page",
                    style = MaterialTheme.typography.titleMedium
                )
            }

        }
    }
}

@Composable
fun EpisodeListItemCard(episode: Episode, onClick: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = episode.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = episode.episode, style = MaterialTheme.typography.titleMedium)
            Icon(
                Icons.Default.ArrowForward,
                modifier = Modifier.padding(16.dp),
                contentDescription = null
            )
        }
        Divider()
    }
}