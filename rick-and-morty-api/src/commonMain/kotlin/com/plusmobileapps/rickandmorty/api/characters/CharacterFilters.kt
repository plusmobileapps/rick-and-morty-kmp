package com.plusmobileapps.rickandmorty.api.characters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CharacterStatus(val apiName: String) {

    @SerialName("Alive")
    ALIVE("alive"),

    @SerialName("Dead")
    DEAD("dead"),

    @SerialName("unknown")
    UNKNOWN("unknown");

    companion object {
        internal const val QUERY_PARAM = "status"
    }

}

@Serializable
enum class CharacterGender(val apiName: String) {

    @SerialName("female")
    FEMALE("female"),

    @SerialName("male")
    MALE("male"),

    @SerialName("genderless")
    GENDERLESS("genderless"),

    @SerialName("unknown")
    UNKNOWN("unknown");

    companion object {
        internal const val QUERY_PARAM = "gender"
    }

}