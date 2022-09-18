package com.plusmobileapps.rickandmorty.androidapp.ui

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.plusmobileapps.rickandmorty.characters.detail.CharacterDetailBloc

@Composable
fun CharacterDetailScreen(bloc: CharacterDetailBloc) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Character detail") },
                navigationIcon = {
                    IconButton(onClick = bloc::onBackClicked) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) {

    }
}