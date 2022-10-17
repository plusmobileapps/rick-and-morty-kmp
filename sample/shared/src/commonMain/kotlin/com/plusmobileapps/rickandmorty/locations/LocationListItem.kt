package com.plusmobileapps.rickandmorty.locations

import com.plusmobileapps.rickandmorty.api.locations.Location

sealed class LocationListItem {
    data class LocationItem(val value: Location) : LocationListItem()
    object NextPageLoading : LocationListItem()

}