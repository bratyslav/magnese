package com.example.magnise.data.price

import com.example.magnise.model.Instrument
import com.example.magnise.model.Price

class PriceRepository(
    private val priceDataSource: PriceDataSource,
) {

    suspend fun getInstrumentList(token: String): List<Instrument> {
        return try {
            val list = priceDataSource.getInstrumentList(token)
            PriceCache.updateInstruments(list)
            list
        } catch (e: Exception) {
            PriceCache.getInstruments()
        }
    }

    suspend fun getPriceHistory(token: String, instrument: Instrument): List<Price> {
        return try {
            val history = priceDataSource.getPriceHistory(token, instrument)
            PriceCache.updatePriceHistory(history)
            history
        } catch (e: Exception) {
            PriceCache.getPriceHistory(instrument)
        }
    }

    fun startLiveInstrumentsConnection(
        token: String,
        onSubscribeListener: () -> Unit,
        onErrorListener: (t: Throwable) -> Unit
    ) {
        priceDataSource.startLiveInstrumentsConnection(token, onSubscribeListener, onErrorListener)
    }

    fun subscribeToInstrument(instrument: Instrument, onMessage: (Price) -> Unit) {
        priceDataSource.subscribeToInstrument(instrument, onMessage)
    }

    fun closeLiveInstrumentsConnection() {
        priceDataSource.closeLiveInstrumentsConnection()
    }

}