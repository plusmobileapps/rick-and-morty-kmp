package com.plusmobileapps.rickandmorty.androidapp.util

import androidx.compose.runtime.Composable
import com.plusmobileapps.paging.PageLoaderException

@Composable
fun PageLoaderException.getUserMessage(): String {
    return when (this) {
        is PageLoaderException.GeneralError -> message
            ?: "Your request has fallen into a portal and not sure where it went, please try again."
        is PageLoaderException.NoNetworkException -> "No network connection, please connect to the internet and try again."
        is PageLoaderException.FirstPageErrorWithCachedResults -> "Error refreshing with latest characters, but viewing cached results."
    }
}