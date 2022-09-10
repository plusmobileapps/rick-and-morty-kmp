package com.plusmobileapps.rickandmorty.characters

import kotlinx.serialization.Serializable

@Serializable
data class CharactersResponse(
    val info: CharactersInfo,
    val results: List<CharacterDTO>
)

@Serializable
data class CharactersInfo(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?
)

@Serializable
data class CharacterDTO(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val origin: CharacterOrigin,
    val image: String,
    val episode: List<String>,
    val url: String,
    val created: String
)

@Serializable
data class CharacterOrigin(
    val name: String,
    val url: String
)