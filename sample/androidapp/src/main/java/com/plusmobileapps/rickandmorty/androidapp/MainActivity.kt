package com.plusmobileapps.rickandmorty.androidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.stackAnimation
import com.plusmobileapps.rickandmorty.RickAndMorty
import com.plusmobileapps.rickandmorty.androidapp.ui.BottomNavUI
import com.plusmobileapps.rickandmorty.androidapp.ui.theme.Rick_and_Morty_KMPTheme
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.db.DriverFactory
import com.plusmobileapps.rickandmorty.root.RootBloc
import com.plusmobileapps.rickandmorty.root.buildRootBloc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Rick_and_Morty_KMPTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootScreen(
                        buildRootBloc(
                            defaultComponentContext(),
                            DriverFactory(LocalContext.current.applicationContext)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun RootScreen(bloc: RootBloc) {
    Children(stack = bloc.routerState, animation = stackAnimation(slide())) {
        when (val child = it.instance) {
            is RootBloc.Child.BottomNav -> BottomNavUI(bloc = child.bloc)
        }
    }
}

@Composable
fun CharactersScreen(state: CharactersViewModel.State) {
    when (state) {
        is CharactersViewModel.State.Error -> Text(text = state.error)
        is CharactersViewModel.State.Loaded -> CharactersList(characters = state.characters)
        CharactersViewModel.State.Loading -> CircularProgressIndicator()
    }
}

@Composable
fun CharactersList(characters: List<RickAndMortyCharacter>) {
    if (characters.isEmpty()) {
        Text(text = "No characters found")
    } else {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(characters) {
                CharacterListItem(character = it) { /* TODO handle click*/ }
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

class CharactersViewModel : ViewModel() {

    sealed class State {
        object Loading : State()
        data class Loaded(val characters: List<RickAndMortyCharacter>) : State()
        data class Error(val error: String) : State()
    }

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Loading)
    val state: StateFlow<State> = _state

    init {
        viewModelScope.launch {
            RickAndMorty.instance.charactersRepository.apply {
                getCharacters().collect { characters ->
                    _state.value = State.Loaded(characters)
                }
                loadNextPage()
            }
        }
    }

}