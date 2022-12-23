package com.plusmobileapps.rickandmorty.androidapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.plusmobileapps.paging.PageLoaderException
import com.plusmobileapps.rickandmorty.androidapp.util.getUserMessage

@Composable
fun FirstPageLoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun FirstPageErrorContent(error: PageLoaderException, onTryAgainClicked: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = error.getUserMessage(),
            style = MaterialTheme.typography.titleLarge
        )
        Button(onClick = onTryAgainClicked) {
            Text("Try again")
        }
    }
}

fun LazyListScope.LoadMoreSection(onLoadMoreClicked: () -> Unit) {
    item("load-more-section") {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = onLoadMoreClicked) {
                Text("Load More")
            }
        }
    }
}

fun LazyListScope.LoadingNextPageSection() {
    item("load-next-page-section") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}

fun LazyListScope.LoadingNextPageErrorSection(error: PageLoaderException, onNextPageTryAgainClicked: () -> Unit) {
    item("loading-next-page-error-section") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = error.getUserMessage())
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNextPageTryAgainClicked) {
                Text("Try again")
            }
        }
    }
}