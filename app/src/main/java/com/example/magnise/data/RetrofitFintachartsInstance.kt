package com.example.magnise.data

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

object RetrofitFintachartsInstance {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://platform.fintacharts.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: FintachartsService by lazy {
        retrofit.create(FintachartsService::class.java)
    }

}

interface FintachartsService {

    @FormUrlEncoded
    @POST("/identity/realms/fintatech/protocol/openid-connect/token")
    suspend fun getToken(
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String,
        @Field("username") username: String,
        @Field("password") password: String
    ): Tokens

    @GET("/api/instruments/v1/instruments")
    suspend fun getInstrumentList(
        @Header("Authorization") token: String,
        @Query("provider") provider: String = "oanda",
        @Query("kind") kind: String = "forex"
    ): InstrumentList

    @GET("/api/bars/v1/bars/count-back")
    suspend fun getPriceHistory(
        @Header("Authorization") token: String,
        @Query("instrumentId") instrumentId: String,
        @Query("provider") provider: String = "simulation",
        @Query("interval") interval: Int = 1,
        @Query("periodicity") periodicity: String = "day",
        @Query("barsCount") barsCount: Int = 10,
    ): PriceHistoryList

}

data class Tokens(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String
)

data class InstrumentList(
    val paging: Paging,
    val data: List<Instrument>
)

data class Paging(
    val page: Int,
    val pages: Int,
    val items: Int
)

data class Instrument(
    val id: String,
    val symbol: String
)

data class PriceHistoryList(
    val data: List<PriceHistory>
)

data class PriceHistory(
    val o: Double, // value
    val t: String  // timestamp
)