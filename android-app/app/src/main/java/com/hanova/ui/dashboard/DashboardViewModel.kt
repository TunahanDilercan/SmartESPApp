package com.hanova.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hanova.data.SensorRepository

class DashboardViewModel : ViewModel() {
    private val repository = SensorRepository.getInstance()
    
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages
    
    // Expose data from repository
    val temperature: LiveData<Int> = repository.temperatureData
    val humidity: LiveData<Int> = repository.humidityData
    val gasStatus: LiveData<String> = repository.gasStatus
    val motionStatus: LiveData<String> = repository.motionStatus
    val parkingStatus: LiveData<String> = repository.parkingStatus
    
    init {
        _messages.value = getDummyMessages()
        // Start data fetching when ViewModel is created
        repository.startDataFetching()
    }      fun processUserCommand(userMessage: String): String {
        val command = userMessage.lowercase()
        return when {
            command.contains("sıcaklık") -> {
                val temp = temperature.value ?: 0
                "Şu anki sıcaklık: ${temp}°C"
            }
            command.contains("nem") -> {
                val hum = humidity.value ?: 0
                "Şu anki nem oranı: ${hum}%"
            }
            command.contains("karbon") || command.contains("gaz") -> {
                gasStatus.value ?: "Bilgi alınamadı"
            }
            command.contains("hareket") -> {
                motionStatus.value ?: "Bilgi alınamadı"
            }
            command.contains("park") || command.contains("otopark") || command.contains("piezo") -> {
                parkingStatus.value ?: "Bilgi alınamadı"
            }
            command.contains("tüm sensör") || command.contains("tüm veri") -> {
                val temp = temperature.value ?: 0
                val hum = humidity.value ?: 0
                val gas = gasStatus.value ?: "Bilgi alınamadı"
                val motion = motionStatus.value ?: "Bilgi alınamadı"
                val parking = parkingStatus.value ?: "Bilgi alınamadı"
                
                """
                📊 Tüm Sensör Verileri:
                🌡️ Sıcaklık: ${temp}°C
                💧 Nem: ${hum}%
                🌫️ Gaz Durumu: $gas
                🏃 Hareket: $motion
                🚗 Piezo: $parking
                """.trimIndent()
            }
            command.contains("ışık aç") || command.contains("led aç") -> {
                repository.setLedState(1) { _ -> }
                "Kırmızı LED açıldı"
            }
            command.contains("ışık kapat") || command.contains("led kapat") -> {
                repository.setLedState(2) { _ -> }
                "Mavi LED açıldı"
            }
            command.contains("komut") -> {
                "Mevcut Komutlar ve İşlevleri:\n" +
                "Sıcaklık ve Nem:\n" +
                "\"Sıcaklık kaç derece?\" veya \"Nem oranı nedir?\"\n" +
                "Gaz Sensörü (MQ2):\n" +
                "\"Karbondioksit algılandı mı?\"\n" +
                "Hareket Sensörü (PIR):\n" +
                "\"Hareket algılandı mı?\" veya \"Ortamda bir hareket var mı?\"\n" +
                "Piezo Sensörü:\n" +
                "\"Piezo durumu nedir?\" veya \"Titreşim algılandı mı?\"\n" +
                "LED Kontrol:\n" +
                "\"Işık aç\" veya \"Işık kapat\"\n" +
                "Tüm Veriler:\n" +
                "\"Tüm sensörleri göster\""
            }
            else -> {
                "Üzgünüm, komutunuzu anlayamadım. Komutları görmek için 'komutlar' yazabilirsiniz."
            }
        }
    }
    
    fun sendUserMessage(content: String) {
        val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
        currentMessages.add(Message("Kullanıcı", content))
        
        val assistantResponse = processUserCommand(content)
        currentMessages.add(Message("Akıllı Ev Asistanı", assistantResponse))
        
        _messages.value = currentMessages
    }
    
    private fun getDummyMessages(): List<Message> {
        return listOf(
            Message("Akıllı Ev Asistanı", "Merhaba! Akıllı ev asistanına hoş geldiniz. Size nasıl yardımcı olabilirim?"),
            Message("Kullanıcı", "Komutlar neler?"),
            Message("Akıllı Ev Asistanı", "Mevcut Komutlar ve İşlevleri:\n" +
                "Sıcaklık ve Nem:\n" +
                "\"Sıcaklık kaç derece?\" veya \"Nem oranı nedir?\"\n" +
                "Gaz Sensörü (MQ2):\n" +
                "\"Karbondioksit algılandı mı?\"\n" +
                "Hareket Sensörü (PIR):\n" +
                "\"Hareket algılandı mı?\" veya \"Ortamda bir hareket var mı?\"\n" +
                "Piezo Sensörü:\n" +
                "\"Piezo durumu nedir?\" veya \"Titreşim algılandı mı?\"\n" +
                "LED Kontrol:\n" +
                "\"Işık aç\" veya \"Işık kapat\"")
        )
    }
}