package com.example.magnise.data.price.fintacharts

import android.util.Log
import com.example.magnise.data.RetrofitFintachartsInstance
import com.example.magnise.data.price.PriceDataSource
import com.example.magnise.model.Currency
import com.example.magnise.model.Instrument
import com.example.magnise.model.Price
import com.example.magnise.util.toTimestampAsIsoOffset
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.math.BigDecimal

class PriceDataSourceImpl : PriceDataSource {

    private val client = OkHttpClient()
    private val gson = Gson()

    private var webSocket: WebSocket? = null
    private var currentInstrument: Instrument? = null
    private var onPriceListener: ((Price) -> Unit)? = null

    override suspend fun getInstrumentList(token: String): List<Instrument> {
        val response = RetrofitFintachartsInstance.api.getInstrumentList("Bearer $token")
        return response.data.map {
            val currencyIds = it.symbol.split("/")
            Instrument(it.id, Currency.getInstance(currencyIds[0]), Currency.getInstance(currencyIds[1]))
        }
    }

    override suspend fun getPriceHistory(token: String, instrument: Instrument): List<Price> {
        val response = RetrofitFintachartsInstance.api.getPriceHistory("Bearer $token", instrument.id)
        return response.data.map {
            Price(instrument, BigDecimal.valueOf(it.o), it.t.toTimestampAsIsoOffset())
        }
    }

    override fun startLiveInstrumentsConnection(
        token: String,
        onSubscribeListener: () -> Unit,
        onErrorListener: (t: Throwable) -> Unit
    ) {
        val request = Request.Builder()
            .url("wss://platform.fintacharts.com/api/streaming/ws/v1/realtime?token=$token")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onMessage(webSocket: WebSocket, text: String) {
                val response = gson.fromJson(text, Response::class.java)

                when(response.type) {
                    "session" -> onSubscribeListener()
                    "l1-snapshot" -> onNewPriceResponse(text)
                    "l1-update" -> onNewPriceResponse(text)
                    else -> {}
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                super.onFailure(webSocket, t, response)
                onErrorListener(t)
            }

        })
    }

    override fun subscribeToInstrument(instrument: Instrument, onMessage: (Price) -> Unit) {
        webSocket?.let {
            currentInstrument = instrument
            onPriceListener = onMessage

            val request = SubscribeRequest(instrument.id)
            it.send(gson.toJson(request))
        }
    }

    override fun closeLiveInstrumentsConnection() {
        webSocket?.close(1000, null)
    }

    private fun onNewPriceResponse(json: String) {
        Log.d("LOGS", json)
        val response = gson.fromJson(json, PriceResponse::class.java)

        currentInstrument?.let { instrument ->
            val data = response.ask ?: response.quote?.ask ?: return@let
            val price = Price(
                instrument,
                BigDecimal.valueOf(data.price),
                data.timestamp.toTimestampAsIsoOffset()
            )

            onPriceListener?.invoke(price)
        }
    }

    data class SubscribeRequest(
        val instrumentId: String,
        val provider: String = "simulation",
        val type: String = "l1-subscription",
        val id: String = "1",
        val subscribe: Boolean = true,
        val kinds: List<String> = listOf("ask")
    )

    data class Response(
        val type: String
    )

    data class PriceResponse(
        val ask: PriceResponseValue?,
        val quote: PriceQuoteResponse?
    )

    data class PriceQuoteResponse(
        val ask: PriceResponseValue,
    )

    data class PriceResponseValue(
        val timestamp: String,
        val price: Double,
        val volume: Int
    )

}