package com.plusmobileapps.rickandmorty.api

import io.ktor.client.engine.*
import io.ktor.client.engine.android.*

internal actual fun createHttpClientEngine(): HttpClientEngine = Android.create()