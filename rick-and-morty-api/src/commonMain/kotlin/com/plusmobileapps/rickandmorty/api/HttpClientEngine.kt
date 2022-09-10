package com.plusmobileapps.rickandmorty.api

import io.ktor.client.engine.*

internal expect fun createHttpClientEngine(): HttpClientEngine