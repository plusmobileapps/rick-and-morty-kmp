package com.plusmobileapps.rickandmorty.db

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(MyDatabase.Schema, "database.db")
    }
}