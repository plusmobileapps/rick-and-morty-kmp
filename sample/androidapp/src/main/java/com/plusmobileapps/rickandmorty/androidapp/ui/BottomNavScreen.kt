package com.plusmobileapps.rickandmorty.androidapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.rickandmorty.bottomnav.BottomNavBloc

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavUI(bloc: BottomNavBloc) {
    Scaffold(bottomBar = {
        BottomNavigationBar(bloc.models, bloc::onNavItemClicked)
    }) { paddingValues ->
        Children(
            bloc.routerState,
            animation = stackAnimation(fade() + scale()),
            modifier = Modifier.padding(paddingValues)
        ) {
            when (val child = it.instance) {
                is BottomNavBloc.Child.Characters -> CharactersUI(bloc = child.bloc)
                is BottomNavBloc.Child.Episodes -> EpisodesUI(bloc = child.bloc)
                is BottomNavBloc.Child.About -> AboutUI()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    models: Value<BottomNavBloc.Model>,
    onClick: (BottomNavBloc.NavItem) -> Unit
) {
    val model = models.subscribeAsState()
    val items = model.value.navItems
    NavigationBar {
        items.forEach {
            NavigationBarItem(
                selected = it.selected,
                onClick = { onClick(it) },
                icon = {
                    Icon(
                        when (it.type) {
                            BottomNavBloc.NavItem.Type.CHARACTERS -> Icons.Default.Person
                            BottomNavBloc.NavItem.Type.EPISODES -> Icons.Default.List
                            BottomNavBloc.NavItem.Type.ABOUT -> Icons.Default.Info
                        },
                        contentDescription = null
                    )
                },
                label = { Text(it.type.toString()) }
            )
        }
    }
}