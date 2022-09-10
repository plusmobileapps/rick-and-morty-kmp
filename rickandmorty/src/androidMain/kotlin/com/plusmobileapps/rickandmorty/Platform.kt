package com.plusmobileapps.rickandmorty

import android.content.Context
import com.plusmobileapps.rickandmorty.db.DriverFactory
import com.plusmobileapps.rickandmorty.db.MyDatabase

actual class Platform actual constructor() {
    actual val platform: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

fun createDatabase(context: Context): MyDatabase =
    com.plusmobileapps.rickandmorty.db.createDatabase(DriverFactory(context))