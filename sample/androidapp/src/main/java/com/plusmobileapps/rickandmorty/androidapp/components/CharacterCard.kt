package com.plusmobileapps.rickandmorty.androidapp.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter

val characterCardWidth: Dp = 150.dp

@Composable
fun CharacterCard(character: RickAndMortyCharacter, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .clickable { onClick() }
            .semantics {
                contentDescription = character.name
            },
    ) {
        Column {
            AsyncImage(
                modifier = Modifier.fillMaxWidth(),
                model = character.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
            Text(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
                text = character.name,
                style = MaterialTheme.typography.titleLarge,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}