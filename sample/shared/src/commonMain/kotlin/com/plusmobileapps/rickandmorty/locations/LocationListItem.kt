package com.plusmobileapps.rickandmorty.locations

import com.plusmobileapps.rickandmorty.api.locations.Location

sealed interface LocationListItem {
    data class LocationItem(val value: Location) : LocationListItem
    object NextPageLoading : LocationListItem

}