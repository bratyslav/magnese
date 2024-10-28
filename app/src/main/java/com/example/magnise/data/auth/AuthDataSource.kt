package com.example.magnise.data.auth

interface AuthDataSource {

    suspend fun getToken(): String

    suspend fun refreshToken(): String

}