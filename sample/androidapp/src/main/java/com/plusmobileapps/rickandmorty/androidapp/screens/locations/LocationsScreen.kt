package com.plusmobileapps.rickandmorty.androidapp.screens.locations

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.plusmobileapps.paging.PageLoaderException
import com.plusmobileapps.rickandmorty.androidapp.components.*
import com.plusmobileapps.rickandmorty.androidapp.util.rememberScrollContext
import com.plusmobileapps.rickandmorty.api.locations.Location
import com.plusmobileapps.rickandmorty.locations.LocationListItem
import com.plusmobileapps.rickandmorty.locations.list.LocationBloc
import kotlinx.coroutines.launch

@Composable
fun LocationsScreen(bloc: LocationBloc) {
    val model = bloc.models.subscribeAsState()
    val lazyListState = rememberLazyListState()

    val showFirstPageErrorWithCachedResultsSnackbar by remember {
        derivedStateOf {
            model.value.pageLoadedError is PageLoaderException.FirstPageErrorWithCachedResults
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Locations") })
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
        LocationsBody(
            modifier = Modifier.padding(it),
            bloc = bloc,
            lazyListState = lazyListState,
        )
    }
}

@Composable
fun LocationsBody(
    modifier: Modifier,
    bloc: LocationBloc,
    lazyListState: LazyListState,
) {
    val model by bloc.models.subscribeAsState()
    val error = model.pageLoadedError

    when {
        error?.isFirstPage == true && error !is PageLoaderException.FirstPageErrorWithCachedResults -> {
            FirstPageErrorContent(
                error = error,
                modifier = modifier,
                onTryAgainClicked = bloc::loadMore,
            )
        }
        model.firstPageIsLoading -> {
            FirstPageLoadingIndicator(modifier)
        }
        else -> {
            LocationsList(
                modifier = modifier,
                lazyListState = lazyListState,
                locations = model.locations,
                nextPageIsLoading = model.nextPageIsLoading,
                hasMoreToLoad = model.hasMoreToLoad,
                error = model.pageLoadedError,
                onLoadNextPage = bloc::loadMore,
                onLocationClicked = bloc::onLocationClicked,
            )
        }
    }
}

@Composable
fun LocationsList(
    modifier: Modifier,
    lazyListState: LazyListState,
    locations: List<Location>,
    nextPageIsLoading: Boolean,
    hasMoreToLoad: Boolean,
    error: PageLoaderException?,
    onLoadNextPage: () -> Unit,
    onLocationClicked: (Location) -> Unit,
) {
    val showError by remember {
        derivedStateOf {
            error != null && !error.isFirstPage
        }
    }
    LazyColumn(modifier = modifier, state = lazyListState) {
        items(locations) { location ->
            LocationListItemCard(location = location) {
                onLocationClicked(location)
            }
        }

        if (nextPageIsLoading) {
            LoadingNextPageSection()
        }

        if (showError) {
            error?.let {
                LoadingNextPageErrorSection(
                    error = it,
                    onNextPageTryAgainClicked = onLoadNextPage,
                )
            }
        }

        if (hasMoreToLoad) {
            LoadMoreSection(onLoadNextPage)
        }
    }
}

@Composable
fun LocationListItemCard(location: Location, onClick: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = location.name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${location.residents.size} residents",
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                Icons.Default.ArrowForward,
                modifier = Modifier.padding(16.dp),
                contentDescription = null
            )
        }
        Divider()
    }
}