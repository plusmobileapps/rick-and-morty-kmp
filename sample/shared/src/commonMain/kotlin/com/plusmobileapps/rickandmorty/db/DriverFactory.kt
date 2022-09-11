package com.plusmobileapps.rickandmorty.db

import com.squareup.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

internal fun createDatabase(driverFactory: DriverFactory): MyDatabase {
    val driver = driverFactory.createDriver()
    return MyDatabase.invoke(driver)
}