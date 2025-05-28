package com.hanova.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hanova.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessagesViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>().apply {
        value = listOf(
            Message("Sistem", "Hoş geldiniz! Cihaza komut göndermek için aşağıdaki giriş kutusunu kullanabilirsiniz.", "09:00:00", false),
            Message("Sistem", "Örnek komutlar:\nled on\nled off\nget temp\nget humidity", "09:00:05", false)
        )    }
    val messages: LiveData<List<Message>> = _messages

    fun sendCommand(command: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val currentList = _messages.value?.toMutableList() ?: mutableListOf()
        
        // Aynı komutu tekrar göndermeyi önle (son 5 saniye içinde)
        val lastMessage = currentList.lastOrNull { it.isFromUser }
        val shouldPreventDuplicate = lastMessage?.let { 
            it.content == command && 
            System.currentTimeMillis() - parseTime(it.timestamp) < 5000 
        } ?: false
        
        if (shouldPreventDuplicate) {
            // Duplicate mesaj uyarısı
            currentList.add(Message("Sistem", "⚠️ Aynı komut kısa süre önce gönderildi", timestamp, false))
            _messages.value = currentList
            return
        }

        // Kullanıcı mesajını ekle
        currentList.add(Message("Sen", command, timestamp, true))

        // Gelişmiş yanıt sistemi
        val response = when {
            command.equals("led on", ignoreCase = true) || 
            command.equals("LED_ON", ignoreCase = true) -> "✅ LED açıldı"
            
            command.equals("led off", ignoreCase = true) || 
            command.equals("LED_OFF", ignoreCase = true) -> "✅ LED kapatıldı"
            
            command.startsWith("get temp", ignoreCase = true) -> "🌡️ Sıcaklık: 25°C"
            command.startsWith("get humidity", ignoreCase = true) -> "💧 Nem: %60"
            command.contains("🎤", ignoreCase = true) -> {
                // Sesli komut, yanıt verme
                return
            }            command.contains("🤖", ignoreCase = true) -> {
                // Asistan yanıtı, tekrar yanıt verme
                return
            }
            else -> "📡 Komut ESP8266'ya gönderildi: $command"
        }

        currentList.add(Message("ESP8266", response, timestamp, false))
        _messages.value = currentList
    }
      fun addMessage(sender: String, content: String, isFromUser: Boolean) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val currentList = _messages.value?.toMutableList() ?: mutableListOf()
        currentList.add(Message(sender, content, timestamp, isFromUser))
        _messages.value = currentList
    }
    
    private fun parseTime(timeStr: String): Long {
        return try {
            val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val timeOnly = format.parse(timeStr)
            timeOnly?.time ?: 0
        } catch (e: Exception) {
            0
        }
    }
}
