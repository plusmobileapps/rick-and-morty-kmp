package com.plusmobileapps.rickandmorty.androidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.stackAnimation
import com.plusmobileapps.rickandmorty.androidapp.ui.*
import com.plusmobileapps.rickandmorty.androidapp.ui.theme.Rick_and_Morty_KMPTheme
import com.plusmobileapps.rickandmorty.db.DriverFactory
import com.plusmobileapps.rickandmorty.root.RootBloc
import com.plusmobileapps.rickandmorty.root.buildRootBloc

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootBloc = buildRootBloc(
            defaultComponentContext(),
            DriverFactory(applicationContext)
        )
        setContent {
            val windowSize = calculateWindowSizeClass(activity = this)
            Rick_and_Morty_KMPTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootScreen(rootBloc, windowSize)
                }
            }
        }
    }
}

@Composable
fun RootScreen(bloc: RootBloc, windowSize: WindowSizeClass) {
    Children(stack = bloc.routerState, animation = stackAnimation(slide())) {
        when (val child = it.instance) {
            is RootBloc.Child.BottomNav -> BottomNavUI(bloc = child.bloc, windowSize)
            is RootBloc.Child.CharacterSearch -> CharacterSearchScreen(bloc = child.bloc)
            is RootBloc.Child.EpisodeSearch -> EpisodeSearchScreen(bloc = child.bloc)
            is RootBloc.Child.CharacterDetail -> CharacterDetailScreen(bloc = child.bloc)
            is RootBloc.Child.EpisodeDetail -> EpisodeDetailScreen(bloc = child.bloc, windowSize)
        }
    }
}