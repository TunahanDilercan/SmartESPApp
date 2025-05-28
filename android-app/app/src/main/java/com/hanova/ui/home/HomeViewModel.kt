package com.hanova.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hanova.data.SensorRepository

class HomeViewModel : ViewModel() {
    private val repository = SensorRepository.getInstance()
    
    // Expose data from repository
    val temperature: LiveData<Int> = repository.temperatureData
    val humidity: LiveData<Int> = repository.humidityData
    val gasStatus: LiveData<String> = repository.gasStatus
    val motionStatus: LiveData<String> = repository.motionStatus
    val parkingStatus: LiveData<String> = repository.parkingStatus
    val errorMessage: LiveData<String> = repository.errorMessage
    val connectionStatus: LiveData<Boolean> = repository.connectionStatus
    
    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage
    
    init {
        // Start data fetching when ViewModel is created
        repository.startDataFetching()
        
        // Observe connection status
        repository.connectionStatus.observeForever { isConnected ->
            if (isConnected) {
                _statusMessage.value = "ESP8266 bağlantısı kuruldu"
            } else {
                _statusMessage.value = "ESP8266 bağlantı hatası!"
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // No need to stop fetching here, as we want continuous updates
        // even when switching fragments
    }
    
    fun setLedState(state: Int, callback: (String) -> Unit) {
        repository.setLedState(state, callback)
    }
}