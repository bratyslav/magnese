package com.example.magnise.data.auth

class AuthRepository(
    private val authDataSource: AuthDataSource
) {

    suspend fun getToken(): String {
        return try {
            authDataSource.getToken()
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun refreshToken(): String {
        return authDataSource.refreshToken()
    }

}