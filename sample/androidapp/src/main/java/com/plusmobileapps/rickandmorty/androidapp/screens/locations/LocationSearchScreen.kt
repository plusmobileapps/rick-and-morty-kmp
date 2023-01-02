@file:OptIn(ExperimentalComposeUiApi::class)

package com.plusmobileapps.rickandmorty.androidapp.screens.locations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.plusmobileapps.paging.PagingDataSourceState
import com.plusmobileapps.rickandmorty.androidapp.R
import com.plusmobileapps.rickandmorty.androidapp.components.*
import com.plusmobileapps.rickandmorty.api.locations.Location
import com.plusmobileapps.rickandmorty.locations.search.LocationSearchBloc

@Composable
fun LocationSearchScreen(bloc: LocationSearchBloc) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            LocationSearchBar(
                onBackClick = bloc::onBackClicked,
                onSearchClick = {
                    keyboardController?.hide()
                    bloc.executeSearchClicked()
                },
                onClearClick = bloc::clearSearchClicked,
            )
        }
    ) { paddingValues ->
        LocationSearchBody(
            modifier = Modifier.padding(paddingValues),
            bloc = bloc
        )
    }
}

@Composable
fun LocationSearchBar(
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onClearClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(text = "Location Search")
        },
        navigationIcon = {
            IconButton(onBackClick, modifier = Modifier.padding(4.dp)) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go Back",
                )
            }
        },
        actions = {
            IconButton(onClick = onClearClick) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear Search",
                )
            }
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search locations",
                )
            }
        }
    )
}

@Composable
fun LocationSearchBody(
    modifier: Modifier,
    bloc: LocationSearchBloc,
) {
    val model = bloc.models.subscribeAsState()
    val error = model.value.pageLoaderState.pageLoaderError

    Column(
        modifier = modifier,
    ) {
        LocationSearchFields(
            model = model.value,
            onLocationSearchClick = bloc::executeSearchClicked,
            onLocationChanged = bloc::onLocationChanged,
            onDimensionChanged = bloc::onDimensionChanged,
            onTypeChanged = bloc::onTypeChanged,
        )
        when {
            model.value.pageLoaderState.isFirstPageLoading -> FirstPageLoadingIndicator()
            error?.isFirstPage == true -> {
                FirstPageErrorContent(
                    error = error,
                    onTryAgainClicked = bloc::loadNextPage,
                )
            }
            model.value.pageLoaderState.data.isEmpty() -> NoLocationSearchResultsContent()
            else -> {
                LocationSearchResultList(
                    pageLoaderState = model.value.pageLoaderState,
                    onLocationClicked = bloc::onLocationClicked,
                    onLoadMore = bloc::loadNextPage,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.LocationSearchFields(
    model: LocationSearchBloc.Model,
    onLocationSearchClick: () -> Unit,
    onLocationChanged: (String) -> Unit,
    onDimensionChanged: (String) -> Unit,
    onTypeChanged: (String) -> Unit,
) {
    var showExtraFilter by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(16.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = model.location,
            onValueChange = onLocationChanged,
            label = { Text("Location") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { onLocationSearchClick() }
            )
        )

        IconButton(onClick = { showExtraFilter = !showExtraFilter }) {
            if (showExtraFilter) {
                Icon(
                    painterResource(id = R.drawable.expand_less),
                    contentDescription = "Close Filters"
                )
            } else {
                Icon(
                    painterResource(id = R.drawable.expand_more),
                    contentDescription = "Open Filters"
                )
            }
        }
    }

    AnimatedVisibility(visible = showExtraFilter) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = model.dimension,
                onValueChange = onDimensionChanged,
                label = { Text("Dimension") }
            )
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = model.type,
                onValueChange = onTypeChanged,
                label = { Text("Type") }
            )
        }
    }
}

@Composable
fun NoLocationSearchResultsContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        androidx.compose.material3.Text(
            text = "No results to display, try to update your query and try again.",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun LocationSearchResultList(
    pageLoaderState: PagingDataSourceState<Location>,
    onLocationClicked: (Location) -> Unit,
    onLoadMore: () -> Unit,
) {
    val error = pageLoaderState.pageLoaderError
    LazyColumn {
        items(pageLoaderState.data, key = { it.id }) {
            LocationListItemCard(location = it, onClick = { onLocationClicked(it) })
        }

        if (pageLoaderState.hasMoreToLoad) {
            LoadMoreSection(onLoadMore)
        }

        if (pageLoaderState.isNextPageLoading) {
            LoadingNextPageSection()
        }

        if (error != null && !error.isFirstPage) {
            LoadingNextPageErrorSection(error, onLoadMore)
        }
    }
}