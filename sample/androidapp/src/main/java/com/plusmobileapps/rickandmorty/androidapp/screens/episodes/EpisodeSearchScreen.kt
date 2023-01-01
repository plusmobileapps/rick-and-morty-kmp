@file:OptIn(ExperimentalComposeUiApi::class)

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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.plusmobileapps.paging.PagingDataSourceState
import com.plusmobileapps.rickandmorty.androidapp.R
import com.plusmobileapps.rickandmorty.androidapp.components.*
import com.plusmobileapps.rickandmorty.api.episodes.Episode
import com.plusmobileapps.rickandmorty.episodes.search.EpisodeSearchBloc

@Composable
fun EpisodeSearchScreen(bloc: EpisodeSearchBloc) {
    val keyboardController = LocalSoftwareKeyboardController.current
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
            onSearchClicked = {
                keyboardController?.hide()
                bloc.onSearchClicked()
            },
            onFiltersClicked = bloc::onFiltersToggleClicked
        )
        AnimatedVisibility(model.value.showFilters) {
            EpisodeSearchExtraFiltersUI(
                episodeCode = model.value.episodeCode,
                episodeCodeChanged = bloc::onEpisodeCodeChanged,
                onClearEpisodeCodeClicked = bloc::onClearEpisodeCodeClicked
            )
        }
        val error = model.value.pageLoaderState.pageLoaderError

        when {
            model.value.pageLoaderState.isFirstPageLoading -> FirstPageLoadingIndicator()
            error?.isFirstPage == true -> {
                FirstPageErrorContent(error = error) { bloc.onFirstPageTryAgainClicked() }
            }
            else -> EpisodeSearchResults(
                modifier = Modifier.weight(1f),
                pageLoadingState = model.value.pageLoaderState,
                onLoadMore = bloc::loadMoreResults,
                onNextPageTryAgainClicked = bloc::onNextPageTryAgainClicked,
                onEpisodeClicked = bloc::onEpisodeClicked
            )
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
    pageLoadingState: PagingDataSourceState<Episode>,
    onLoadMore: () -> Unit,
    onNextPageTryAgainClicked: () -> Unit,
    onEpisodeClicked: (Episode) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        items(pageLoadingState.data) {
            EpisodeListItemCard(episode = it) { onEpisodeClicked(it) }
        }

        if (pageLoadingState.hasMoreToLoad) {
            LoadMoreSection(onLoadMore)
        }

        if (pageLoadingState.isNextPageLoading) {
            LoadingNextPageSection()
        }

        val error = pageLoadingState.pageLoaderError

        if (error != null && !error.isFirstPage) {
            LoadingNextPageErrorSection(error, onNextPageTryAgainClicked)
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