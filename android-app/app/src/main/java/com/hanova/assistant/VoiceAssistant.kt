package com.hanova.assistant

import android.content.Context

class VoiceAssistant(
    private val context: Context,
    private val onCommandReceived: (String) -> Unit,
    private val onSensorDataRequest: (String) -> String?,
    private val onListeningStateChanged: ((Boolean) -> Unit)? = null
) {
    
    private var isListening = false
    
    fun startListening() {
        // Stub implementation - will be enhanced with actual speech recognition
        isListening = true
        onListeningStateChanged?.invoke(true)
        
        // Simulate voice recognition with a test command
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            onCommandReceived("🎤 Test command: hava kaç derece")
            processCommand("hava kaç derece")
            stopListening()
        }, 2000)
    }
    
    fun stopListening() {
        isListening = false
        onListeningStateChanged?.invoke(false)
    }
    
    fun destroy() {
        // Cleanup resources
    }
    
    private fun processCommand(command: String) {
        val lowerCommand = command.lowercase()
        
        val response = when {
            // Sıcaklık sorguları
            lowerCommand.contains("sıcaklık") || 
            lowerCommand.contains("derece") || 
            lowerCommand.contains("kaç derece") ||
            lowerCommand.contains("hava") -> {
                val temp = onSensorDataRequest("temperature")
                if (temp != null) {
                    "🌡️ Sıcaklık: ${temp}°C"
                } else {
                    "❌ Sıcaklık bilgisi alınamadı"
                }
            }
            
            // Nem sorguları
            lowerCommand.contains("nem") -> {
                val humidity = onSensorDataRequest("humidity")
                if (humidity != null) {
                    "💧 Nem: %${humidity}"
                } else {
                    "❌ Nem bilgisi alınamadı"
                }
            }
            
            // Gaz sensörü sorguları
            lowerCommand.contains("gaz") || lowerCommand.contains("hava kalitesi") -> {
                val gas = onSensorDataRequest("gas")
                if (gas != null) {
                    "🌬️ Gaz durumu: $gas"
                } else {
                    "❌ Gaz sensörü bilgisi alınamadı"
                }
            }
            
            // Hareket sensörü sorguları
            lowerCommand.contains("hareket") || lowerCommand.contains("kimse var mı") -> {
                val motion = onSensorDataRequest("motion")
                if (motion != null && motion.contains("Hareket")) {
                    "🚶 Hareket algılandı!"
                } else {
                    "😴 Hareket yok"
                }
            }
            
            // Park sensörü sorguları
            lowerCommand.contains("araba") || lowerCommand.contains("park") || lowerCommand.contains("araç") -> {
                val car = onSensorDataRequest("car")
                if (car != null) {
                    "🚗 Park durumu: $car"
                } else {
                    "❌ Park sensörü bilgisi alınamadı"
                }
            }
            
            // LED kontrol komutları
            lowerCommand.contains("led aç") || lowerCommand.contains("ışık aç") -> {
                onCommandReceived("🤖 💡 LED açıldı")
                return
            }
            
            lowerCommand.contains("led kapat") || lowerCommand.contains("ışık kapat") -> {
                onCommandReceived("🤖 💡 LED kapatıldı")
                return
            }
            
            else -> {
                "❓ Komut anlaşılamadı. Tekrar deneyin."
            }
        }
        
        onCommandReceived("🤖 $response")
    }
}
