package com.plusmobileapps.rickandmorty.util

import platform.Foundation.NSUUID

actual fun randomUUID(): String = NSUUID().UUIDString()