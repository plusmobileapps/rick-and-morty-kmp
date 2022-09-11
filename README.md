# Rick and Morty KMP

A [Kotlin Multiplatform](https://kotlinlang.org/lp/mobile/) project that creates an Http client library around the [Rick and Morty API](https://rickandmortyapi.com/documentation/#rest).

## Setup

The Rick and Morty API client supports both Android & iOS and can be added to the common main dependencies. Replace `$version` with the latest version: 

[![Maven Central](https://img.shields.io/maven-central/v/com.plusmobileapps/rick-and-morty-api?color=blue)](https://search.maven.org/artifact/com.plusmobileapps/rick-and-morty-api)

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

## Resources 

* [How to build a kotlin multiplatform library](https://dev.to/kotlin/how-to-build-and-publish-a-kotlin-multiplatform-library-going-public-4a8k)