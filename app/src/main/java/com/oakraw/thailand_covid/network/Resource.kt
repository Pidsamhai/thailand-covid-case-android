package com.oakraw.thailand_covid.network

sealed class Resource<out T: Any> {
    data class Success<out T: Any>(val data: T) : Resource<T>()
    object Loading: Resource<Nothing>()
    data class Error(val message: String) : Resource<Nothing>()
}