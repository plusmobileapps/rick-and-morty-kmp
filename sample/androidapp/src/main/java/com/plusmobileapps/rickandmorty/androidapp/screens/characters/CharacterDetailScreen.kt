package com.plusmobileapps.rickandmorty.androidapp.screens.characters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.plusmobileapps.rickandmorty.characters.detail.CharacterDetailBloc
import java.util.Locale

@Composable
fun CharacterDetailScreen(bloc: CharacterDetailBloc) {
    val model = bloc.models.subscribeAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(model.value.character.name) },
                navigationIcon = {
                    IconButton(onClick = bloc::onBackClicked) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) {
        CharacterDetailContent(modifier = Modifier.padding(it), state = model.value)
    }
}

@Composable
fun CharacterDetailContent(modifier: Modifier, state: CharacterDetailBloc.Model) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxWidth(),
            model = state.character.imageUrl,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = "Species: ${state.character.species}",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = "Status: ${state.character.status}",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
    }
}