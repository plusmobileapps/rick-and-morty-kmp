package com.plusmobileapps.rickandmorty.api.locations

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
)

@Serializable
data class Location(
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String,
    val residents: List<String>,
    val url: String,
    val created: String,
)