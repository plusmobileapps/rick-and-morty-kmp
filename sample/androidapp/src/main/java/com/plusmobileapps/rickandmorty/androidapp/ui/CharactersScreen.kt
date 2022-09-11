package com.plusmobileapps.rickandmorty.androidapp.ui

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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.plusmobileapps.rickandmorty.androidapp.util.rememberScrollContext
import com.plusmobileapps.rickandmorty.characters.CharactersBloc
import com.plusmobileapps.rickandmorty.characters.CharactersListItem
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import kotlinx.coroutines.launch

@Composable
fun CharactersUI(bloc: CharactersBloc) {
    val model = bloc.models.subscribeAsState()
    val lazyListState = rememberLazyListState()

    val scope = rememberCoroutineScope()
    val scrollContext = rememberScrollContext(lazyListState)

    if (scrollContext.isBottom) {
        bloc.loadMoreCharacters()
    }

    Scaffold(
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
    lazyListState: LazyListState,
    characters: List<CharactersListItem>,
    onCharacterClicked: (RickAndMortyCharacter) -> Unit,
) {
    LazyColumn(modifier = Modifier.padding(paddingValues), state = lazyListState) {
        items(characters, key = {
            when (it) {
                is CharactersListItem.Character -> it.value.id
                is CharactersListItem.PageLoading -> CharactersListItem.PageLoading.KEY
            }
        }) {
            when (it) {
                is CharactersListItem.Character -> CharacterListItem(character = it.value) {
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
fun CharacterListItem(character: RickAndMortyCharacter, onClick: () -> Unit) {
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
        Text(modifier = Modifier.weight(1f), text = character.name, style = MaterialTheme.typography.titleMedium)
        Icon(
            Icons.Default.ArrowForward,
            modifier = Modifier.padding(16.dp),
            contentDescription = null
        )
    }
}