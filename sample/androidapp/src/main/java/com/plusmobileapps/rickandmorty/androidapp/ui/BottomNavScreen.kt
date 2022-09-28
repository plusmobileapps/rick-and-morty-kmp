package com.plusmobileapps.rickandmorty.androidapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.rickandmorty.androidapp.ui.components.SideShadow
import com.plusmobileapps.rickandmorty.bottomnav.BottomNavBloc

@Composable
fun BottomNavUI(bloc: BottomNavBloc, windowSize: WindowSizeClass) {
    when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Compact -> BottomNavigationUI(bloc)
        else -> NavigationRailUI(bloc = bloc)
    }
}

@Composable
private fun NavigationRailUI(bloc: BottomNavBloc) {
    val model = bloc.models.subscribeAsState()
    Row(Modifier.fillMaxWidth()) {
        NavigationRail(
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            model.value.navItems.forEach {
                NavigationRailItem(
                    modifier = Modifier.padding(8.dp),
                    selected = it.selected,
                    onClick = { bloc.onNavItemClicked(it) },
                    icon = {
                        Icon(it.type.toIcon(), contentDescription = null)
                    },
                    label = { Text(it.type.toString()) }
                )
            }
        }
        SideShadow(alpha = .1f)
        NavigationScreenContent(bloc = bloc)
    }
}

@Composable
fun NavigationScreenContent(
    bloc: BottomNavBloc,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    Children(
        bloc.routerState,
        animation = stackAnimation(fade() + scale()),
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        when (val child = it.instance) {
            is BottomNavBloc.Child.Characters -> CharactersUI(bloc = child.bloc)
            is BottomNavBloc.Child.Episodes -> EpisodesUI(bloc = child.bloc)
            is BottomNavBloc.Child.About -> AboutUI()
            is BottomNavBloc.Child.Locations -> LocationsUI(bloc = child.bloc)
        }
    }
}

@Composable
private fun BottomNavigationUI(bloc: BottomNavBloc) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(bloc.models, bloc::onNavItemClicked)
        }
    ) { paddingValues ->
        Children(
            bloc.routerState,
            animation = stackAnimation(fade() + scale()),
            modifier = Modifier.padding(paddingValues)
        ) {
            when (val child = it.instance) {
                is BottomNavBloc.Child.Characters -> CharactersUI(bloc = child.bloc)
                is BottomNavBloc.Child.Episodes -> EpisodesUI(bloc = child.bloc)
                is BottomNavBloc.Child.About -> AboutUI()
                is BottomNavBloc.Child.Locations -> LocationsUI(bloc = child.bloc)
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
    NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
        items.forEach {
            NavigationBarItem(
                selected = it.selected,
                onClick = { onClick(it) },
                icon = {
                    Icon(it.type.toIcon(), contentDescription = null)
                },
                label = { Text(it.type.toString()) }
            )
        }
    }
}

private fun BottomNavBloc.NavItem.Type.toIcon() = when (this) {
    BottomNavBloc.NavItem.Type.CHARACTERS -> Icons.Default.Person
    BottomNavBloc.NavItem.Type.EPISODES -> Icons.Default.List
    BottomNavBloc.NavItem.Type.ABOUT -> Icons.Default.Info
    BottomNavBloc.NavItem.Type.LOCATIONS -> Icons.Default.LocationOn
}