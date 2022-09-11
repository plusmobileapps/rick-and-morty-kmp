package com.plusmobileapps.rickandmorty.androidapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.rickandmorty.androidapp.ui.theme.Rick_and_Morty_KMPTheme
import com.plusmobileapps.rickandmorty.characters.CharactersBloc
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter

@Composable
fun CharactersUI(bloc: CharactersBloc) {
    val model = bloc.models.subscribeAsState()
    val characters = model.value.characters

    LazyColumn {
        items(characters) {
            CharacterListItem(character = it) { bloc.onCharacterClicked(it) }
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
        AsyncImage(model = character.imageUrl, contentDescription = null)
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

@Preview
@Composable
fun CharactersUIPreview() {
    Rick_and_Morty_KMPTheme() {
        Surface {
            CharactersUI(bloc = object : CharactersBloc {
                override val models: Value<CharactersBloc.Model> =
                    MutableValue(
                        CharactersBloc.Model(
                            characters = listOf(
                                RickAndMortyCharacter(
                                    0,
                                    "Rick Sanchez",
                                    "https://rickandmortyapi.com/api/character/avatar/377.jpeg"
                                ),
                                RickAndMortyCharacter(1, "Morty", "Some Image")
                            )
                        )
                    )

                override fun onCharacterClicked(character: RickAndMortyCharacter) {
                    TODO("Not yet implemented")
                }
            })
        }
    }
}