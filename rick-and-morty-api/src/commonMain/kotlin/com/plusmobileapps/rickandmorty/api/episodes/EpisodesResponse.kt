package com.plusmobileapps.rickandmorty.api.episodes

import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class EpisodesResponse(
    val info: EpisodesInfo,
    val results: List<Episode>
)

@Serializable
data class EpisodesInfo(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?
) {
    val nextPageNumber: String? by lazy {
        next?.let {
            Url(it).parameters["page"]
        }
    }
}

@Serializable
data class Episode(
    val id: Int = 0,
    val name: String = "",
    val air_date: String = "",
    val episode: String = "",
    val characters: List<String> = emptyList(),
    val url: String = "",
    val created: String = ""
)