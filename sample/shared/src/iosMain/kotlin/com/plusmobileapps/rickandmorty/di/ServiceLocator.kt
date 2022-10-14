package com.plusmobileapps.rickandmorty.di

import com.plusmobileapps.rickandmorty.db.DriverFactory

fun buildServiceLocator(): ServiceLocator =
    ServiceLocator(DriverFactory())