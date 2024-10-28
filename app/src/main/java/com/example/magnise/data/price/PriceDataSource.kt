package com.example.magnise.data.price

import com.example.magnise.model.Instrument
import com.example.magnise.model.Price

interface PriceDataSource {

    suspend fun getInstrumentList(token: String): List<Instrument>

    suspend fun getPriceHistory(token: String, instrument: Instrument): List<Price>

    fun startLiveInstrumentsConnection(
        token: String,
        onSubscribeListener: () -> Unit,
        onErrorListener: (t: Throwable) -> Unit
    )

    fun subscribeToInstrument(instrument: Instrument, onMessage: (Price) -> Unit)

    fun closeLiveInstrumentsConnection()

}