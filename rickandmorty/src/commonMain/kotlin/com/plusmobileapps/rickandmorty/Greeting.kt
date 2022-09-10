package com.plusmobileapps.rickandmorty

class Greeting {
    fun greeting(): String {
        return "Hello, ${Platform().platform}!"
    }
}