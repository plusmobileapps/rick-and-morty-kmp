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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.plusmobileapps.rickandmorty.androidapp.util.rememberScrollContext
import com.plusmobileapps.rickandmorty.api.locations.Location
import com.plusmobileapps.rickandmorty.locations.LocationListItem
import com.plusmobileapps.rickandmorty.locations.list.LocationBloc
import kotlinx.coroutines.launch

@Composable
fun LocationsUI(bloc: LocationBloc) {
    val model = bloc.models.subscribeAsState()
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val scrollContext = rememberScrollContext(listState = lazyListState)

    if (scrollContext.isBottom) bloc.loadMore()

    Scaffold(
        topBar = {
            SmallTopAppBar(title = { Text(text = "Locations") }, actions = {
                IconButton(onClick = bloc::onSearchClicked) {
                    Icon(Icons.Default.Search, contentDescription = "Search Locations")
                }
            })
        },
        floatingActionButton = {
            AnimatedVisibility(visible = !scrollContext.isBottom) {
                FloatingActionButton(onClick = {
                    scope.launch {
                        lazyListState.animateScrollToItem(model.value.locations.lastIndex)
                    }
                }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
            }
        }
    ) {
        LocationsList(
            modifier = Modifier.padding(it),
            lazyListState = lazyListState,
            locations = model.value.locations,
            onLocationClicked = bloc::onLocationClicked
        )
    }
}

@Composable
fun LocationsList(
    modifier: Modifier,
    lazyListState: LazyListState,
    locations: List<LocationListItem>,
    onLocationClicked: (Location) -> Unit,
) {
    LazyColumn(modifier = modifier, state = lazyListState) {
        items(locations) { location ->
            when (location) {
                is LocationListItem.LocationItem -> LocationListItemCard(location = location.value) {
                    onLocationClicked(location.value)
                }
                LocationListItem.NextPageLoading -> CircularProgressIndicator()
            }
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