package com.plusmobileapps.rickandmorty.androidapp.screens.characters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
        model.pageLoadedError?.isFirstPage == true -> {
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(model.pageLoadedError!!.getUserMessage(), style = MaterialTheme.typography.titleLarge)
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
    error: PageLoaderException?,
    onCharacterClicked: (RickAndMortyCharacter) -> Unit,
    onLoadNextPage: () -> Unit,
) {
    val scrollContext = rememberScrollContext(lazyListState)

    if (scrollContext.isBottom) {
        onLoadNextPage()
    }
    LazyVerticalGrid(
        modifier = modifier,
        state = lazyListState,
        columns = GridCells.Adaptive(characterCardWidth)
    ) {
        items(characters, key = { it.id }) {
            CharacterCard(character = it) { onCharacterClicked(it) }
        }

        item("character-next-page-loading") {
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

        item("character-page-loading-error") {
            AnimatedVisibility(visible = showError) {
                Column(
                    modifier = modifier.padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(error?.getUserMessage() ?: "An error occured", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onLoadNextPage) {
                        Text(text = "Try again")
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