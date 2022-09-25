package com.plusmobileapps.rickandmorty.androidapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.rickandmorty.androidapp.R
import com.plusmobileapps.rickandmorty.androidapp.ui.components.SearchFilterDropdown
import com.plusmobileapps.rickandmorty.androidapp.ui.theme.Rick_and_Morty_KMPTheme
import com.plusmobileapps.rickandmorty.api.characters.CharacterGender
import com.plusmobileapps.rickandmorty.api.characters.CharacterStatus
import com.plusmobileapps.rickandmorty.api.characters.CharacterStatus.ALIVE
import com.plusmobileapps.rickandmorty.characters.CharactersListItem
import com.plusmobileapps.rickandmorty.characters.RickAndMortyCharacter
import com.plusmobileapps.rickandmorty.characters.search.CharacterSearchBloc

@Composable
fun CharacterSearchScreen(bloc: CharacterSearchBloc) {
    val model = bloc.models.subscribeAsState()
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchBar(
            query = model.value.query,
            showFilters = model.value.showFilters,
            onBackClicked = bloc::onBackClicked,
            onQueryChanged = bloc::onQueryChanged,
            onSearchClicked = bloc::onSearchClicked,
            onFiltersClicked = bloc::onFiltersClicked
        )
        AnimatedVisibility(model.value.showFilters) {
            FiltersBottomSheet(
                gender = model.value.gender,
                onGenderClicked = bloc::onGenderChanged,
                onClearGenderClicked = bloc::onClearGenderClicked,
                status = model.value.status,
                onStatusClicked = bloc::onStatusChanged,
                onClearStatusClicked = bloc::onClearStatusClicked,
                species = model.value.species,
                onSpeciesChanged = bloc::onSpeciesChanged,
            )
        }
        AnimatedVisibility(model.value.isLoading) {
            CircularProgressIndicator()
        }
        val items = model.value.results
        if (items.isEmpty()) {
            Text(modifier = Modifier.weight(1f), text = "No results")
        } else {
            CharacterSearchResults(
                modifier = Modifier.weight(1f),
                characters = items,
                onCharacterClicked = { })
        }
    }

}

@Composable
fun SearchBar(
    query: String,
    showFilters: Boolean,
    onBackClicked: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onSearchClicked: () -> Unit,
    onFiltersClicked: () -> Unit
) {
    SmallTopAppBar(
        modifier = Modifier.shadow(4.dp),
        navigationIcon = {
            IconButton(onBackClicked, modifier = Modifier.padding(4.dp)) {
                Icon(Icons.Default.ArrowBack, "Go Back")
            }
        },
        title = {
            TextField(
                modifier = Modifier.padding(4.dp),
                value = query,
                onValueChange = onQueryChanged,
                label = { Text("Character name") },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(onSearch = { onSearchClicked() })
            )
        },
        actions = {
            IconButton(onClick = onSearchClicked) {
                Icon(Icons.Default.Search, contentDescription = "Search characters")
            }
            IconButton(onClick = onFiltersClicked) {
                if (showFilters) {
                    Icon(Icons.Default.Close, contentDescription = "Close Filters")
                } else {
                    Icon(
                        painterResource(id = R.drawable.ic_tune),
                        contentDescription = "Open Filters"
                    )
                }
            }
        }
    )
}

@Composable
fun CharacterSearchResults(
    modifier: Modifier,
    characters: List<CharactersListItem>,
    onCharacterClicked: (RickAndMortyCharacter) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(characters) {
            when (it) {
                is CharactersListItem.Character -> CharacterListItemCard(
                    character = it.value,
                    onClick = { onCharacterClicked(it.value) })
                is CharactersListItem.PageLoading -> TODO()
            }

        }
    }
}

@Composable
fun FiltersBottomSheet(
    gender: CharacterGender?,
    onGenderClicked: (CharacterGender) -> Unit,
    onClearGenderClicked: () -> Unit,
    status: CharacterStatus?,
    onStatusClicked: (CharacterStatus) -> Unit,
    onClearStatusClicked: () -> Unit,
    species: String,
    onSpeciesChanged: (String) -> Unit,
) {
    Column {
        Row {
            SearchFilterDropdown(
                modifier = Modifier.weight(1f),
                selected = gender?.name,
                contentDescription = "CLear gender",
                items = CharacterGender.values().toList(),
                itemLabel = { it.name },
                onItemSelected = onGenderClicked,
                onClearClicked = onClearGenderClicked
            )
            SearchFilterDropdown(
                modifier = Modifier.weight(1f),
                selected = status?.name,
                contentDescription = "Clear status",
                items = CharacterStatus.values().toList(),
                itemLabel = { it.name },
                onItemSelected = onStatusClicked,
                onClearClicked = onClearStatusClicked
            )
        }
        TextField(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            value = species,
            onValueChange = onSpeciesChanged,
            label = { Text("Species") },
            trailingIcon = {
                IconButton(onClick = { onSpeciesChanged("") }) {
                    Icon(Icons.Default.Clear, "Clear Species")
                }
            })
    }
}

@Preview
@Composable
fun CharacterSearchPreview() {
    var showFilters by remember {
        mutableStateOf(false)
    }
    Rick_and_Morty_KMPTheme {
        Surface {
            CharacterSearchScreen(bloc = object : CharacterSearchBloc {
                override val models: Value<CharacterSearchBloc.Model> =
                    MutableValue(
                        CharacterSearchBloc.Model(
                            isLoading = true,
                            query = "",
                            results = listOf(
                                CharactersListItem.Character(
                                    RickAndMortyCharacter(name = "Pickle Rick")
                                ),
                                CharactersListItem.PageLoading(isLoading = true, hasMore = false),
                            ),
                            status = ALIVE,
                            species = "",
                            gender = CharacterGender.MALE,
                            error = null,
                            showFilters = showFilters,
                        )
                    )

                override fun onClearSearch() {
                    TODO("Not yet implemented")
                }

                override fun onSearchClicked() {
                    TODO("Not yet implemented")
                }

                override fun onQueryChanged(query: String) {
                    TODO("Not yet implemented")
                }

                override fun onClearQueryClicked() {
                    TODO("Not yet implemented")
                }

                override fun onStatusChanged(status: com.plusmobileapps.rickandmorty.api.characters.CharacterStatus) {
                    TODO("Not yet implemented")
                }

                override fun onClearStatusClicked() {
                    TODO("Not yet implemented")
                }

                override fun onSpeciesChanged(species: String) {
                    TODO("Not yet implemented")
                }

                override fun onClearSpeciesClicked() {
                    TODO("Not yet implemented")
                }

                override fun onGenderChanged(gender: com.plusmobileapps.rickandmorty.api.characters.CharacterGender) {
                    TODO("Not yet implemented")
                }

                override fun onClearGenderClicked() {
                    TODO("Not yet implemented")
                }

                override fun onFiltersClicked() {
                    showFilters = !showFilters
                }

                override fun onBackClicked() {
                    TODO("Not yet implemented")
                }
            })
        }
    }
}