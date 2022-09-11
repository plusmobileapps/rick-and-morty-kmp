package com.plusmobileapps.rickandmorty.androidapp

import android.os.Bundle
import android.os.Message
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.plusmobileapps.rickandmorty.RickAndMorty
import com.plusmobileapps.rickandmorty.androidapp.ui.theme.Rick_and_Morty_KMPTheme
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
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
                    val viewModel: CharactersViewModel by viewModels()
                    val state = viewModel.state.collectAsState()
                    CharactersScreen(state = state.value)
                }
            }
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
        Text(modifier = Modifier.weight(1f), text = character.name, style = MaterialTheme.typography.titleMedium)
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
            RickAndMorty.instance.charactersStore.apply {
                getCharacters().collect { characters ->
                    _state.value = State.Loaded(characters)
                }
                loadNextPage()
            }
        }
    }

}