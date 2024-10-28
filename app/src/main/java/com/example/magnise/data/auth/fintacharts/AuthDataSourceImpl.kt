package com.example.magnise.data.auth.fintacharts

import com.example.magnise.BuildConfig
import com.example.magnise.data.RetrofitFintachartsInstance
import com.example.magnise.data.auth.AuthDataSource

class AuthDataSourceImpl : AuthDataSource {

    override suspend fun getToken(): String {
        val response = RetrofitFintachartsInstance.api.getToken(
            "password",
            "app-cli",
            "r_test@fintatech.com",
            BuildConfig.MGS_API_KEY
        )
        return response.accessToken
    }

    override suspend fun refreshToken(): String {
        // todo
        return ""
    }

}