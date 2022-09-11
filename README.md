# Rick and Morty KMP

A [Kotlin Multiplatform](https://kotlinlang.org/lp/mobile/) project that creates an Http client library around the [Rick and Morty API](https://rickandmortyapi.com/documentation/#rest).

## Setup

The Rick and Morty API client supports both Android & iOS and can be added to the common main dependencies. Please check [releases](https://github.com/plusmobileapps/rick-and-morty-kmp/releases) for the latest version.

```kotlin
buildScript {
    repositories {
        mavenCentral()
    }
}

commonMain {
    dependencies {
        implementation("com.plusmobileapps:rick-and-morty-api:$version")
    }
}
```

## Usage 

The API client itself can be accessed through `RickAndMortyApi.instance` where all of the following methods can be used. 

```kotlin
// in a coroutine 
val characters = RickAndMortyApi.instance.getCharacters(page = 1)
```

## Libraries Used 

### API Client

* [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) - async
* [Ktor](https://ktor.io/) - http client
* [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) - JSON serialization