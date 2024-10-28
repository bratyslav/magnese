package com.example.magnise.util

sealed class Loading<T> {

    class InProgress<T> : Loading<T>()

    class Done<T>(val value: T) : Loading<T>()

}