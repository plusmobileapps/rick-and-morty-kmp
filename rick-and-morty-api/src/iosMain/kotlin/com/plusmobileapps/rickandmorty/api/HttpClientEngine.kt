package com.plusmobileapps.rickandmorty.api

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

internal actual fun createHttpClientEngine(): HttpClientEngine = Darwin.create()