package com.hanova.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Repository pattern class that centralizes all data operations for the ESP8266 device
 */
class SensorRepository {
    // Configure OkHttpClient with timeouts
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
        
    private val _temperatureData = MutableLiveData<Int>()
    private val _humidityData = MutableLiveData<Int>()
    private val _gasStatus = MutableLiveData<String>()
    private val _motionStatus = MutableLiveData<String>()
    private val _parkingStatus = MutableLiveData<String>()
    private val _isDataFetching = MutableLiveData<Boolean>(false)
    private val _errorMessage = MutableLiveData<String>()
    private val _connectionStatus = MutableLiveData<Boolean>(false)

    // Public LiveData exposed to ViewModels
    val temperatureData: LiveData<Int> = _temperatureData
    val humidityData: LiveData<Int> = _humidityData
    val gasStatus: LiveData<String> = _gasStatus
    val motionStatus: LiveData<String> = _motionStatus
    val parkingStatus: LiveData<String> = _parkingStatus
    val isDataFetching: LiveData<Boolean> = _isDataFetching
    val errorMessage: LiveData<String> = _errorMessage
    val connectionStatus: LiveData<Boolean> = _connectionStatus

    private var fetchJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    
    // Track active requests to avoid duplicates
    private val activeRequests = mutableSetOf<String>()
    
    // Cache data in case of temporary connection loss
    private var cachedTemperature: Int? = null
    private var cachedHumidity: Int? = null
    private var cachedGasStatus: String? = null
    private var cachedMotionStatus: String? = null
    private var cachedParkingStatus: String? = null
    
    // Track connection failures
    private var consecutiveFailures = 0
    private val maxConsecutiveFailures = 3

    companion object {
        private const val BASE_URL = "http://192.168.4.1"
        private const val TAG = "SensorRepository"
        
        // Singleton instance
        @Volatile
        private var INSTANCE: SensorRepository? = null
        
        fun getInstance(): SensorRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = SensorRepository()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Starts continuous data fetching in a coroutine
     */
    fun startDataFetching() {
        if (fetchJob?.isActive == true) return
        
        _isDataFetching.postValue(true)
        
        fetchJob = scope.launch {
            while (isActive) {
                try {
                    // Use cached values if we've had too many consecutive failures
                    if (consecutiveFailures >= maxConsecutiveFailures) {
                        delay(5000) // Wait 5 seconds before retrying
                        fetchSensorData() // Try one more time
                    } else {
                        fetchSensorData()
                        delay(800) // Small delay between different API calls
                        fetchMotionStatus() 
                        delay(800)
                        fetchParkingStatus()
                    }
                    
                    delay(3000) // 3 seconds between complete refresh cycles
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching data: ${e.message}")
                    _errorMessage.postValue("Network error: ${e.message}")
                    consecutiveFailures++
                    delay(5000) // Wait longer after an error
                }
            }
        }
    }

    /**
     * Stops data fetching
     */
    fun stopDataFetching() {
        fetchJob?.cancel()
        _isDataFetching.postValue(false)
        
        // Cancel any active requests
        synchronized(activeRequests) {
            activeRequests.clear()
        }
    }

    /**
     * Fetches temperature and humidity data
     */
    private fun fetchSensorData() {
        val endpoint = "$BASE_URL/data"
        
        // Skip if a request to this endpoint is already active
        synchronized(activeRequests) {
            if (activeRequests.contains(endpoint)) return
            activeRequests.add(endpoint)
        }
        
        val request = Request.Builder().url(endpoint).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                synchronized(activeRequests) {
                    activeRequests.remove(endpoint)
                }
                consecutiveFailures++
                _connectionStatus.postValue(false)
                
                if (consecutiveFailures == 1) {
                    _errorMessage.postValue("Bağlantı hatası: ${e.message}")
                }                // Use cached values if available
                cachedTemperature?.let { 
                    _temperatureData.postValue(it)
                    Log.d(TAG, "DHT sensör hatası - önbellek sıcaklık kullanılıyor: $it")
                } ?: run {
                    _temperatureData.postValue(22) // Varsayılan değer
                    Log.w(TAG, "DHT sensör hatası - varsayılan sıcaklık kullanılıyor: 22")
                }
                
                cachedHumidity?.let { 
                    _humidityData.postValue(it)
                    Log.d(TAG, "DHT sensör hatası - önbellek nem kullanılıyor: $it")
                } ?: run {
                    _humidityData.postValue(60) // Varsayılan değer
                    Log.w(TAG, "DHT sensör hatası - varsayılan nem kullanılıyor: 60")
                }
                
                cachedGasStatus?.let { 
                    _gasStatus.postValue(it)
                    Log.d(TAG, "Gaz sensör hatası - önbellek kullanılıyor: $it")
                } ?: run {
                    _gasStatus.postValue("Normal") // Varsayılan değer
                    Log.w(TAG, "Gaz sensör hatası - varsayılan durum kullanılıyor: Normal")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                synchronized(activeRequests) {
                    activeRequests.remove(endpoint)
                }
                
                consecutiveFailures = 0
                _connectionStatus.postValue(true)
                
                val jsonResponse = response.body?.string()
                if (jsonResponse != null) {
                    try {
                        val jsonObject = JSONObject(jsonResponse)
                        val temperature = jsonObject.optDouble("temperature", Double.NaN)
                        val humidity = jsonObject.optDouble("humidity", Double.NaN)
                        val gasStatus = jsonObject.optString("status", "Karbondioksit Algilanmadi")

                        if (!temperature.isNaN()) {
                            val tempInt = temperature.toInt()
                            _temperatureData.postValue(tempInt)
                            cachedTemperature = tempInt
                        }
                        
                        if (!humidity.isNaN()) {
                            val humInt = humidity.toInt()
                            _humidityData.postValue(humInt)
                            cachedHumidity = humInt
                        }
                        
                        _gasStatus.postValue(gasStatus)
                        cachedGasStatus = gasStatus
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing sensor data: ${e.message}")
                    }
                }
            }
        })
    }

    /**
     * Fetches motion sensor status
     */
    private fun fetchMotionStatus() {
        val endpoint = "$BASE_URL/pir"
        
        // Skip if a request to this endpoint is already active
        synchronized(activeRequests) {
            if (activeRequests.contains(endpoint)) return
            activeRequests.add(endpoint)
        }
        
        val request = Request.Builder().url(endpoint).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                synchronized(activeRequests) {
                    activeRequests.remove(endpoint)
                }                  // Use cached value if available
                cachedMotionStatus?.let { 
                    _motionStatus.postValue(it)
                    Log.d(TAG, "PIR sensör hatası - önbellek kullanılıyor: $it")
                } ?: run {
                    _motionStatus.postValue("Hareket Yok") // Varsayılan değer
                    Log.w(TAG, "PIR sensör hatası - varsayılan durum kullanılıyor: Hareket Yok")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                synchronized(activeRequests) {
                    activeRequests.remove(endpoint)
                }
                
                val jsonResponse = response.body?.string()
                if (jsonResponse != null) {
                    try {
                        val jsonObject = JSONObject(jsonResponse)
                        val message = jsonObject.optString("message", "No data")
                        _motionStatus.postValue(message)
                        cachedMotionStatus = message
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing motion data: ${e.message}")
                    }
                }
            }
        })
    }

    /**
     * Fetches parking status
     */
    private fun fetchParkingStatus() {
        val endpoint = "$BASE_URL/piezo"
        
        // Skip if a request to this endpoint is already active
        synchronized(activeRequests) {
            if (activeRequests.contains(endpoint)) return
            activeRequests.add(endpoint)
        }
        
        val request = Request.Builder().url(endpoint).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                synchronized(activeRequests) {
                    activeRequests.remove(endpoint)
                }                  // Use cached value if available
                cachedParkingStatus?.let { 
                    _parkingStatus.postValue(it)
                    Log.d(TAG, "Piezo sensör hatası - önbellek kullanılıyor: $it")
                } ?: run {
                    _parkingStatus.postValue("Boş") // Varsayılan değer
                    Log.w(TAG, "Piezo sensör hatası - varsayılan durum kullanılıyor: Boş")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                synchronized(activeRequests) {
                    activeRequests.remove(endpoint)
                }
                
                val jsonResponse = response.body?.string()
                if (jsonResponse != null) {
                    try {
                        val jsonObject = JSONObject(jsonResponse)
                        val message = jsonObject.optString("message", "No data")
                        _parkingStatus.postValue(message)
                        cachedParkingStatus = message
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing parking data: ${e.message}")
                    }
                }
            }
        })
    }

    /**
     * Controls the LED state
     * @param state 1 for red LED, 2 for blue LED
     */
    fun setLedState(state: Int, callback: (String) -> Unit) {
        val request = Request.Builder()
            .url("$BASE_URL/led?state=$state")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _errorMessage.postValue("Failed to control LED: ${e.message}")
                callback("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: "No Response"
                callback(responseBody)
            }
        })
    }
}
