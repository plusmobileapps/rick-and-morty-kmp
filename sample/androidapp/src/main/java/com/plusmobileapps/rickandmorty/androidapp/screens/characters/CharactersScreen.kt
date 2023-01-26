package com.plusmobileapps.rickandmorty.androidapp.screens.characters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.plusmobileapps.paging.PageLoaderException
import com.plusmobileapps.rickandmorty.androidapp.components.CharacterCard
import com.plusmobileapps.rickandmorty.androidapp.components.characterCardWidth
import com.plusmobileapps.rickandmorty.androidapp.util.getUserMessage
import com.plusmobileapps.rickandmorty.androidapp.util.rememberScrollContext
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.characters.list.CharactersBloc
import kotlinx.coroutines.launch

@Composable
fun CharactersUI(bloc: CharactersBloc) {
    val model = bloc.models.subscribeAsState()
    val lazyListState = rememberLazyGridState()

    val showFirstPageErrorWithCachedResultsSnackbar by remember {
        derivedStateOf {
            model.value.pageLoadedError is PageLoaderException.FirstPageErrorWithCachedResults
        }
    }

    val scope = rememberCoroutineScope()
    val scrollContext = rememberScrollContext(lazyListState)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Characters") }, actions = {
                IconButton(onClick = bloc::onSearchClicked) {
                    Icon(Icons.Default.Search, contentDescription = "Search Characters")
                }
            })
        },
        floatingActionButton = {
            AnimatedVisibility(visible = !scrollContext.isBottom) {
                FloatingActionButton(onClick = {
                    scope.launch {
                        lazyListState.animateScrollToItem(model.value.characters.lastIndex)
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
                        TextButton(onClick = { bloc.loadMoreCharacters() }) {
                            Text("Refresh", color = MaterialTheme.colorScheme.background)
                        }
                    },
                ) {
                    Text(text = "Couldn't load the first page, but viewing cached results")
                }
            }
        }
    ) {
        CharactersUIBody(
            modifier = Modifier.padding(it),
            bloc = bloc,
            lazyListState = lazyListState,
        )
    }


}

@Composable
private fun CharactersUIBody(
    modifier: Modifier,
    bloc: CharactersBloc,
    lazyListState: LazyGridState
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
                Button(onClick = bloc::loadMoreCharacters) {
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
            CharactersList(
                modifier = modifier,
                lazyListState = lazyListState,
                characters = model.characters,
                onCharacterClicked = bloc::onCharacterClicked,
                nextPageIsLoading = model.nextPageIsLoading,
                hasMoreToLoad = model.hasMoreToLoad,
                error = model.pageLoadedError,
                onLoadNextPage = bloc::loadMoreCharacters,
            )
        }
    }
}

@Composable
fun CharactersList(
    modifier: Modifier,
    lazyListState: LazyGridState,
    characters: List<RickAndMortyCharacter>,
    nextPageIsLoading: Boolean,
    hasMoreToLoad: Boolean,
    error: PageLoaderException?,
    onCharacterClicked: (RickAndMortyCharacter) -> Unit,
    onLoadNextPage: () -> Unit,
) {
    if (characters.isEmpty()) return

    LazyVerticalGrid(
        modifier = modifier,
        state = lazyListState,
        columns = GridCells.Adaptive(characterCardWidth)
    ) {
        items(characters, key = { it.id }) {
            Box(modifier = Modifier.aspectRatio(1f)) {
                CharacterCard(character = it) { onCharacterClicked(it) }
            }
        }

        item(
            key = "character-next-page-loading",
            span = {
                GridItemSpan(maxLineSpan)
            }
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
            span = {
                GridItemSpan(maxLineSpan)
            }
        ) {
            AnimatedVisibility(visible = showError) {
                Column(
                    modifier = modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = spacedBy(16.dp),
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
            span = {
                GridItemSpan(maxLineSpan)
            }
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
fun CharacterListItemCard(character: RickAndMortyCharacter, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AsyncImage(
            modifier = Modifier.size(120.dp),
            model = character.imageUrl,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = character.name,
            style = MaterialTheme.typography.titleMedium
        )
        Icon(
            Icons.Default.ArrowForward,
            modifier = Modifier.padding(16.dp),
            contentDescription = null
        )
    }
}