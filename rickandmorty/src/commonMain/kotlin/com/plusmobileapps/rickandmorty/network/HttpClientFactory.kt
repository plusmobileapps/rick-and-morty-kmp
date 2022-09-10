package com.plusmobileapps.rickandmorty.network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

interface HttpClientFactory {
    fun createHttpClient(): HttpClient
}

internal class HttpClientFactoryImpl : HttpClientFactory {
    override fun createHttpClient(): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    isLenient = true
                    prettyPrint = true
                    ignoreUnknownKeys = true
                }
            )
        }

        defaultRequest {
            url("https://rickandmortyapi.com/")
        }
    }
}