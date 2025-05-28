package com.hanova.ui.messages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class VoiceAssistant(
    private val context: Context,
    private val onCommandReceived: (String) -> Unit,
    private val onSensorDataRequest: (String) -> String?,
    private val onListeningStateChanged: ((Boolean) -> Unit)? = null
) : RecognitionListener {

    // ESP8266 IP address - Access Point mode
    private val ESP8266_IP = "192.168.4.1" // ESP8266 Access Point IP address
      private var isListening = false
    private var speechRecognizer: SpeechRecognizer? = null
    private val TAG = "VoiceAssistant"
    
    // Test modu - gerçek sesli tanıma olmadan test için
    private var isTestMode = false
    
    fun setTestMode(enabled: Boolean) {
        isTestMode = enabled
        Log.d(TAG, "Test modu: ${if (enabled) "Açık" else "Kapalı"}")
    }    fun startListening() {
        if (isListening) return
        
        isListening = true
        onListeningStateChanged?.invoke(true)
        
        // Test modu aktifse simüle edilmiş komutlar kullan
        if (isTestMode) {
            startTestListening()
            return
        }
        
        // Initialize speech recognizer
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(this)
        }
        
        // Check if speech recognition is available
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onCommandReceived("❌ Sesli tanıma bu cihazda desteklenmiyor - Test modu kullanılıyor")
            startTestListening()
            return
        }
        
        // Create recognition intent
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR") // Turkish language
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
        }
        
        Log.d(TAG, "Sesli tanıma başlatılıyor...")
        onCommandReceived("🎤 Dinliyorum... Konuşmaya başlayın")
        
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Sesli tanıma başlatma hatası: ${e.message}")
            onCommandReceived("❌ Sesli tanıma başlatılamadı: ${e.message}")
            stopListening()
        }
    }
    
    private fun startTestListening() {
        // Test komutları - gerçek sesli tanıma olmadan
        val testCommands = listOf(
            "hava kaç derece",
            "sıcaklığı artır", 
            "sıcaklığı azalt",
            "nem oranı nedir",
            "gaz durumu nedir",
            "hareket var mı",
            "park durumu nedir"
        )
        val randomCommand = testCommands.random()
        
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            onCommandReceived("🎤 Test: \"$randomCommand\"")
            
            // Process the command and get sensor data
            processVoiceCommand(randomCommand)
            stopListening()
        }, 2000)
    }
    
    fun stopListening() {
        isListening = false
        onListeningStateChanged?.invoke(false)
    }
      fun destroy() {
        stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
    
    // RecognitionListener implementation
    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "Konuşmaya hazır")
        onCommandReceived("🎤 Hazır - Konuşun...")
    }
    
    override fun onBeginningOfSpeech() {
        Log.d(TAG, "Konuşma başladı")
        onCommandReceived("🎤 Konuşma algılandı...")
    }
    
    override fun onRmsChanged(rmsdB: Float) {
        // Ses seviyesi değişimi - isteğe bağlı olarak kullanılabilir
    }
    
    override fun onBufferReceived(buffer: ByteArray?) {
        // Ses buffer'ı alındı
    }
    
    override fun onEndOfSpeech() {
        Log.d(TAG, "Konuşma bitti")
        onCommandReceived("🎤 Konuşma bitti, işleniyor...")
    }
    
    override fun onError(error: Int) {
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Ses kayıt hatası"
            SpeechRecognizer.ERROR_CLIENT -> "İstemci hatası"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mikrofon izni gerekli"
            SpeechRecognizer.ERROR_NETWORK -> "Ağ hatası"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Ağ zaman aşımı"
            SpeechRecognizer.ERROR_NO_MATCH -> "Konuşma anlaşılamadı, tekrar deneyin"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Tanıma servisi meşgul"
            SpeechRecognizer.ERROR_SERVER -> "Sunucu hatası"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Konuşma zaman aşımı"
            else -> "Bilinmeyen hata: $error"
        }
        
        Log.e(TAG, "Sesli tanıma hatası: $errorMessage")
        onCommandReceived("❌ $errorMessage")
        stopListening()
    }
    
    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val spokenText = matches[0].lowercase(Locale.getDefault())
            Log.d(TAG, "Tanınan metin: $spokenText")
            
            onCommandReceived("🎤 \"$spokenText\"")
            
            // Process the recognized command
            processVoiceCommand(spokenText)
        } else {
            onCommandReceived("❌ Konuşma anlaşılamadı")
        }
        stopListening()
    }
      override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val partialText = matches[0]
            Log.d(TAG, "Kısmi sonuç: $partialText")
            // İsteğe bağlı: kısmi sonuçları göster
            // onCommandReceived("🎤 \"$partialText...\"")
        }
    }
    
    override fun onEvent(eventType: Int, params: Bundle?) {
        // RecognitionListener interface'inin gerekli metodu
        Log.d(TAG, "Sesli tanıma olayı: $eventType")
    }
    fun processVoiceCommand(command: String) {
        when {
            // Temperature increase commands - Red LED (state=1)
            command.contains("sıcaklığı artır") || command.contains("sıcaklık artır") || 
            command.contains("ısıtma aç") || command.contains("sıcak yap") || 
            command.contains("temperature increase") || command.contains("heat up") -> {
                val response = "🔴 Sıcaklık artırılıyor... Kırmızı LED açık!"
                sendLedCommand("1") // Red LED ON, Blue LED OFF
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    onCommandReceived(response)
                }, 1000)
            }
            
            // Temperature decrease commands - Blue LED (state=2)
            command.contains("sıcaklığı azalt") || command.contains("sıcaklık azalt") || 
            command.contains("soğutma aç") || command.contains("soğuk yap") || 
            command.contains("temperature decrease") || command.contains("cool down") -> {
                val response = "🔵 Sıcaklık azaltılıyor... Mavi LED açık!"
                sendLedCommand("2") // Blue LED ON, Red LED OFF
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    onCommandReceived(response)
                }, 1000)
            }
              // Temperature query commands
            command.contains("sıcaklık") || command.contains("derece") || command.contains("temperature") -> {
                val temperature = onSensorDataRequest("temperature")
                val response = if (temperature != null && temperature != "22") {
                    "🌡️ Şu anki sıcaklık: ${temperature}°C"
                } else if (temperature == "22") {
                    "🌡️ Sıcaklık: ${temperature}°C (sensör bağlantısı kontrol edilsin)"
                } else {
                    "❌ Sıcaklık verisi alınamadı - DHT11 sensör bağlantısını kontrol edin"
                }
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    onCommandReceived(response)
                }, 1000)
            }            command.contains("nem") || command.contains("humidity") -> {
                val humidity = onSensorDataRequest("humidity")
                val response = if (humidity != null && humidity != "60") {
                    "💧 Şu anki nem: %${humidity}"
                } else if (humidity == "60") {
                    "💧 Nem: %${humidity} (sensör bağlantısı kontrol edilsin)"
                } else {
                    "❌ Nem verisi alınamadı - DHT11 sensör bağlantısını kontrol edin"
                }
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    onCommandReceived(response)
                }, 1000)
            }            command.contains("gaz") || command.contains("gas") -> {
                val gasStatus = onSensorDataRequest("gas")
                val response = if (gasStatus != null && gasStatus != "Normal") {
                    "🌬️ Gaz durumu: ${gasStatus}"
                } else if (gasStatus == "Normal") {
                    "🌬️ Gaz durumu: ${gasStatus} (MQ2 sensör bağlantısı kontrol edilsin)"
                } else {
                    "❌ Gaz sensor verisi alınamadı - MQ2 sensör bağlantısını kontrol edin"
                }
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    onCommandReceived(response)
                }, 1000)
            }            command.contains("hareket") || command.contains("motion") -> {
                val motionStatus = onSensorDataRequest("motion")
                val response = if (motionStatus != null && motionStatus != "Hareket Yok") {
                    "🚶 Hareket durumu: ${motionStatus}"
                } else if (motionStatus == "Hareket Yok") {
                    "🚶 Hareket durumu: ${motionStatus} (PIR sensör bağlantısı kontrol edilsin)"
                } else {
                    "❌ Hareket sensor verisi alınamadı - PIR sensör bağlantısını kontrol edin"
                }
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    onCommandReceived(response)
                }, 1000)
            }            command.contains("araba") || command.contains("park") || command.contains("car") -> {
                val parkingStatus = onSensorDataRequest("car")
                val response = if (parkingStatus != null && parkingStatus != "Boş") {
                    "🚗 Park durumu: ${parkingStatus}"
                } else if (parkingStatus == "Boş") {
                    "🚗 Park durumu: ${parkingStatus} (Piezo sensör bağlantısı kontrol edilsin)"
                } else {
                    "❌ Park sensor verisi alınamadı - Piezo sensör bağlantısını kontrol edin"
                }
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    onCommandReceived(response)
                }, 1000)
            }else -> {
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    onCommandReceived("❓ Anlaşılmadı. Sıcaklık, nem, gaz, hareket veya park hakkında soru sorabilirsiniz.")
                }, 1000)
            }
        }
    }
    
    private fun sendLedCommand(state: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("http://$ESP8266_IP/led?state=$state")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        when (state) {
                            "1" -> onCommandReceived("✅ $response")
                            "2" -> onCommandReceived("✅ $response")
                        }
                    }
                } else {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        onCommandReceived("❌ LED komutu başarısız: HTTP $responseCode")
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onCommandReceived("❌ ESP8266 bağlantı hatası: ${e.message}")
                }
            }
        }
    }
}
