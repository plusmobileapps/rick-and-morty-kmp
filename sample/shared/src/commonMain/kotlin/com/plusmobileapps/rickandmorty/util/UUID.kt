package com.plusmobileapps.rickandmorty.util

expect fun randomUUID(): String

interface UuidUtil {
    fun randomUuid(): String
}

internal class UuidUtilImpl : UuidUtil {
    override fun randomUuid(): String = randomUUID()
}