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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.plusmobileapps.rickandmorty.androidapp.components.CharacterCard
import com.plusmobileapps.rickandmorty.androidapp.components.characterCardWidth
import com.plusmobileapps.rickandmorty.androidapp.util.rememberScrollContext
import com.plusmobileapps.rickandmorty.characters.CharactersListItem
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.characters.list.CharactersBloc
import kotlinx.coroutines.launch

@Composable
fun CharactersUI(bloc: CharactersBloc) {
    val model = bloc.models.subscribeAsState()
    val lazyListState = rememberLazyGridState()

    val scope = rememberCoroutineScope()
    val scrollContext = rememberScrollContext(lazyListState)

    if (scrollContext.isBottom) {
        bloc.loadMoreCharacters()
    }

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
                        lazyListState.animateScrollToItem(model.value.listItems.lastIndex)
                    }
                }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
            }
        }
    ) {
        CharactersList(
            paddingValues = it,
            lazyListState = lazyListState,
            characters = model.value.listItems,
            onCharacterClicked = bloc::onCharacterClicked
        )
    }


}

@Composable
fun CharactersList(
    paddingValues: PaddingValues,
    lazyListState: LazyGridState,
    characters: List<CharactersListItem>,
    onCharacterClicked: (RickAndMortyCharacter) -> Unit,
) {
    LazyVerticalGrid(
        modifier = Modifier.padding(paddingValues),
        state = lazyListState,
        columns = GridCells.Adaptive(characterCardWidth)
    ) {
        items(characters, key = {
            when (it) {
                is CharactersListItem.Character -> it.value.id
                is CharactersListItem.PageLoading -> CharactersListItem.PageLoading.KEY
            }
        }) {
            when (it) {
                is CharactersListItem.Character -> CharacterCard(character = it.value) {
                    onCharacterClicked(it.value)
                }
                is CharactersListItem.PageLoading -> Text(
                    "Loading another page",
                    style = MaterialTheme.typography.titleMedium
                )
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