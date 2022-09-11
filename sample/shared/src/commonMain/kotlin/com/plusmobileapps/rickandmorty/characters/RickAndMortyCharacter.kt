package com.plusmobileapps.rickandmorty.characters

import com.plusmobileapps.rickandmorty.api.characters.CharacterDTO

data class RickAndMortyCharacter(
    val id: Int = 0,
    val name: String = "",
    val imageUrl: String = "",
    val status: String = "",
    val species: String = ""
) {
    companion object {
        fun fromDTO(dto: CharacterDTO): RickAndMortyCharacter =
            RickAndMortyCharacter(
                id = dto.id,
                name = dto.name,
                imageUrl = dto.image,
                status = dto.status.toString(),
                species = dto.species
            )
    }
}