package com.plusmobileapps.rickandmorty.androidapp.screens.episodes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.plusmobileapps.paging.PageLoaderException
import com.plusmobileapps.rickandmorty.androidapp.util.getUserMessage
import com.plusmobileapps.rickandmorty.androidapp.util.rememberScrollContext
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.episodes.EpisodeListItem
import com.plusmobileapps.rickandmorty.episodes.list.EpisodesBloc
import kotlinx.coroutines.launch

@Composable
fun EpisodesUI(bloc: EpisodesBloc) {
    val model = bloc.models.subscribeAsState()
    val lazyListState = rememberLazyListState()
    val showFirstPageErrorWithCachedResultsSnackbar by remember {
        derivedStateOf {
            model.value.pageLoadedError is PageLoaderException.FirstPageErrorWithCachedResults
        }
    }
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
        },
        bottomBar = {
            AnimatedVisibility(visible = showFirstPageErrorWithCachedResultsSnackbar) {
                Snackbar(
                    action = {
                        TextButton(onClick = { bloc.loadMore() }) {
                            Text("Refresh", color = MaterialTheme.colorScheme.background)
                        }
                    },
                ) {
                    Text(text = "Couldn't load the first page, but viewing cached results")
                }
            }
        }
    ) {
        EpisodesUIBody(modifier = Modifier.padding(it), bloc = bloc, lazyListState = lazyListState)
    }
}

@Composable
private fun EpisodesUIBody(
    modifier: Modifier,
    bloc: EpisodesBloc,
    lazyListState: LazyListState,
) {
    val model by bloc.models.subscribeAsState()
    when {
        model.pageLoadedError?.isFirstPage == true && model.pageLoadedError !is PageLoaderException.FirstPageErrorWithCachedResults -> {
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    model.pageLoadedError!!.getUserMessage(),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = bloc::loadMore) {
                    Text(text = "Try again")
                }
            }
        }
        model.firstPageIsLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        else -> {
            EpisodesList(
                modifier = modifier,
                lazyListState = lazyListState,
                episodes = model.episodes,
                nextPageIsLoading = model.nextPageIsLoading,
                hasMoreToLoad = model.hasMoreToLoad,
                error = model.pageLoadedError,
                onLoadNextPage = bloc::loadMore,
                onEpisodeClicked = bloc::onEpisodeClicked
            )
        }
    }
}

@Composable
fun EpisodesList(
    modifier: Modifier,
    lazyListState: LazyListState,
    episodes: List<Episode>,
    nextPageIsLoading: Boolean,
    hasMoreToLoad: Boolean,
    error: PageLoaderException?,
    onEpisodeClicked: (Episode) -> Unit,
    onLoadNextPage: () -> Unit,
) {
    LazyColumn(modifier = modifier, state = lazyListState) {
        items(episodes, key = { it.id }) {
            EpisodeListItemCard(episode = it) {
                onEpisodeClicked(it)
            }
        }

        item(
            key = "character-next-page-loading",
        ) {
            AnimatedVisibility(visible = nextPageIsLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        val showError = error != null && !error.isFirstPage

        item(
            key = "character-page-loading-error",
        ) {
            AnimatedVisibility(visible = showError) {
                Column(
                    modifier = modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        error?.getUserMessage() ?: "An error occured",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Button(onClick = onLoadNextPage) {
                        Text(text = "Try again")
                    }
                }
            }
        }

        item(
            key = "character-next-page-load-section",
        ) {
            AnimatedVisibility(visible = hasMoreToLoad && !nextPageIsLoading && !showError) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(onClick = onLoadNextPage) {
                        Text(text = "Load more")
                    }
                }
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