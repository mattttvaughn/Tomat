package io.github.mattpvaughn.tomat.util

sealed class NetworkState {
    data class ERROR(val message: String): NetworkState()
    object LOADED: NetworkState()
    object INITIALIZED: NetworkState()
    object LOADING: NetworkState()
}