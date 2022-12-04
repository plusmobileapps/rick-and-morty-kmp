package com.plusmobileapps.rickandmorty.api.locations

import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class LocationsResponse(
    val info: LocationInfo,
    val results: List<Location>,
)

@Serializable
data class LocationInfo(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?,
) {
    val nextPageNumber: String? by lazy {
        next?.let {
            Url(it).parameters["page"]
        }
    }
}

@Serializable
data class Location(
    val id: Int = 0,
    val name: String = "",
    val type: String = "",
    val dimension: String = "",
    val residents: List<String> = emptyList(),
    val url: String = "",
    val created: String = "",
)