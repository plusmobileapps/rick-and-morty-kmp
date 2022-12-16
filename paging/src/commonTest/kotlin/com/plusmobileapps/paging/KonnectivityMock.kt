package com.plusmobileapps.paging

import com.plusmobileapps.konnectivity.Konnectivity
import com.plusmobileapps.konnectivity.NetworkConnection
import kotlinx.coroutines.flow.StateFlow

class KonnectivityMock : Konnectivity {

    private var isConnectedMock: () -> Boolean = { false }

    fun everyIsConnected(mock: () -> Boolean) {
        isConnectedMock = mock
    }

    override val currentNetworkConnection: NetworkConnection
        get() = TODO("Not yet implemented")
    override val currentNetworkConnectionState: StateFlow<NetworkConnection>
        get() = TODO("Not yet implemented")
    override val isConnected: Boolean
        get() = isConnectedMock()
    override val isConnectedState: StateFlow<Boolean>
        get() = TODO("Not yet implemented")
}