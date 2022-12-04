package com.plusmobileapps.rickandmorty.util

import java.util.UUID

actual fun randomUUID(): String = UUID.randomUUID().toString()
