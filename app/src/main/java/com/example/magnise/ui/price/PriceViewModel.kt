package com.example.magnise.ui.price

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.magnise.EncryptedPrefs
import com.example.magnise.util.Loading
import com.example.magnise.data.auth.AuthRepository
import com.example.magnise.data.auth.fintacharts.AuthDataSourceImpl
import com.example.magnise.data.price.PriceRepository
import com.example.magnise.data.price.fintacharts.PriceDataSourceImpl
import com.example.magnise.model.Instrument
import com.example.magnise.model.Price
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PriceViewModel(
    private val authRepository: AuthRepository,
    private val priceRepository: PriceRepository,
    private val encryptedPrefs: EncryptedPrefs
) : ViewModel() {

    private val _instrumentListState = MutableStateFlow<Loading<List<Instrument>>>(Loading.Done(listOf()))
    val instrumentListState: StateFlow<Loading<List<Instrument>>> = _instrumentListState.asStateFlow()

    private val _instrumentState = MutableStateFlow<Instrument?>(null)
    val instrumentState: StateFlow<Instrument?> = _instrumentState.asStateFlow()

    private val _priceState = MutableStateFlow<Price?>(null)
    val priceState: StateFlow<Price?> = _priceState.asStateFlow()

    private val _priceHistoryState = MutableStateFlow<Loading<List<Price>>>(Loading.Done(listOf()))
    val priceHistoryState: StateFlow<Loading<List<Price>>> = _priceHistoryState.asStateFlow()

    override fun onCleared() {
        super.onCleared()
        priceRepository.closeLiveInstrumentsConnection()
    }

    fun init() {
        _instrumentListState.value = Loading.InProgress()
        viewModelScope.launch(Dispatchers.IO) {
            encryptedPrefs.token = authRepository.getToken()
            val list = priceRepository.getInstrumentList(encryptedPrefs.token)
            _instrumentListState.value = Loading.Done(list)
        }
    }

    fun setCurrentInstrument(instrument: Instrument) {
        _instrumentState.value = instrument
    }

    fun subscribeToCurrentInstrument() {
        _instrumentState.value?.let { instrument ->
            priceRepository.closeLiveInstrumentsConnection()
            priceRepository.startLiveInstrumentsConnection(
                encryptedPrefs.token,
                onSubscribeListener = { setInstrument(instrument) },
                onErrorListener = { fetchHistory(instrument) } // try to fetch history cache
            )
        }
    }

    private fun setInstrument(instrument: Instrument) {
        viewModelScope.launch(Dispatchers.IO) {
            priceRepository.subscribeToInstrument(instrument) {
                _priceState.value = it
            }
            fetchHistory(instrument)
        }
    }

    private fun fetchHistory(instrument: Instrument) {
        _priceHistoryState.value = Loading.InProgress()
        viewModelScope.launch(Dispatchers.IO) {
            val history = priceRepository.getPriceHistory(encryptedPrefs.token, instrument)
            _priceHistoryState.value = Loading.Done(history)
        }
    }

    class Factory(
        private val context: Context,
        private val authRepository: AuthRepository = AuthRepository(AuthDataSourceImpl()),
        private val priceRepository: PriceRepository = PriceRepository(PriceDataSourceImpl())
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PriceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PriceViewModel(authRepository, priceRepository, EncryptedPrefs(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}