package com.hanova.ui.messages

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hanova.databinding.FragmentMessagesBinding
import com.hanova.voice.VoiceAssistant
import com.hanova.data.SensorRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessagesFragment : Fragment() {
    
    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private lateinit var messagesViewModel: MessagesViewModel
    private lateinit var adapter: MessagesAdapter
    private lateinit var voiceAssistant: VoiceAssistant
    private lateinit var sensorRepository: SensorRepository
    
    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        messagesViewModel = ViewModelProvider(this).get(MessagesViewModel::class.java)
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        setupRecyclerView()
        setupSendButton()
        setupVoiceButton()
        setupVoiceAssistant()
        
        // Gözlemcileri ayarla
        messagesViewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.updateMessages(messages)
            binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
        }

        return root
    }
    
    private fun setupRecyclerView() {
        adapter = MessagesAdapter()
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        binding.messagesRecyclerView.adapter = adapter
    }
    
    private fun setupSendButton() {
        binding.sendCommandButton.setOnClickListener {
            val command = binding.commandEditText.text.toString().trim()
            if (command.isNotEmpty()) {
                // Komutu gönder ve mesaj listesine ekle
                messagesViewModel.sendCommand(command)
                binding.commandEditText.text?.clear()
            } else {
                Toast.makeText(context, "Lütfen bir komut girin", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupVoiceButton() {
        binding.voiceCommandButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_PERMISSION
                )
            } else {
                voiceAssistant.startListening()
            }
        }
    }      private fun setupVoiceAssistant() {
        // SensorRepository'yi başlat
        sensorRepository = SensorRepository()
          // VoiceAssistant'ı başlat
        voiceAssistant = VoiceAssistant(
            context = requireContext(),            onCommandReceived = { command: String ->
                // Sesli komutları sendCommand ile işle (addMessage gibi)
                messagesViewModel.sendCommand("🎤 $command")
            },
            onSensorDataRequest = { sensorType: String ->
                // Sensör verilerini al
                getSensorData(sensorType)
            },
            onListeningStateChanged = { isListening: Boolean ->
                // Mikrofon butonu renk değişimi
                val colorRes = if (isListening) android.R.color.holo_red_dark else com.hanova.R.color.purple_500
                binding.voiceCommandButton.backgroundTintList = 
                    ContextCompat.getColorStateList(requireContext(), colorRes)
            }
        )
        
        // Test modu - gerçek sesli tanıma yoksa aktif olur
        // Cihazda Google sesli tanıma servisi varsa otomatik olarak gerçek mod kullanılır
        voiceAssistant.setTestMode(false) // false = gerçek sesli tanıma dene, yoksa test moduna geç
    }
      private fun getSensorData(sensorType: String): String? {
        return when (sensorType) {
            "temperature" -> {
                // HomeViewModel'den sıcaklık verisini al
                try {
                    val homeViewModel = ViewModelProvider(requireActivity()).get(com.hanova.ui.home.HomeViewModel::class.java)
                    val temperature = homeViewModel.temperature.value
                    if (temperature != null && temperature > 0) {
                        temperature.toString()
                    } else {
                        "22" // Varsayılan değer - sensör bağlı değil
                    }
                } catch (e: Exception) {
                    Log.w("MessagesFragment", "Sıcaklık verisi alınamadı: ${e.message}")
                    "22" // Varsayılan değer
                }
            }
            "humidity" -> {
                try {
                    val homeViewModel = ViewModelProvider(requireActivity()).get(com.hanova.ui.home.HomeViewModel::class.java)
                    val humidity = homeViewModel.humidity.value
                    if (humidity != null && humidity > 0) {
                        humidity.toString()
                    } else {
                        "60" // Varsayılan değer - sensör bağlı değil
                    }
                } catch (e: Exception) {
                    Log.w("MessagesFragment", "Nem verisi alınamadı: ${e.message}")
                    "60" // Varsayılan değer
                }
            }
            "gas" -> {
                try {
                    val homeViewModel = ViewModelProvider(requireActivity()).get(com.hanova.ui.home.HomeViewModel::class.java)
                    val gasStatus = homeViewModel.gasStatus.value
                    if (!gasStatus.isNullOrEmpty()) {
                        gasStatus
                    } else {
                        "Normal" // Varsayılan değer - sensör bağlı değil
                    }
                } catch (e: Exception) {
                    Log.w("MessagesFragment", "Gaz sensörü verisi alınamadı: ${e.message}")
                    "Normal" // Varsayılan değer
                }
            }
            "motion" -> {
                try {
                    val homeViewModel = ViewModelProvider(requireActivity()).get(com.hanova.ui.home.HomeViewModel::class.java)
                    val motionStatus = homeViewModel.motionStatus.value
                    if (!motionStatus.isNullOrEmpty()) {
                        motionStatus
                    } else {
                        "Hareket Yok" // Varsayılan değer - sensör bağlı değil
                    }
                } catch (e: Exception) {
                    Log.w("MessagesFragment", "Hareket sensörü verisi alınamadı: ${e.message}")
                    "Hareket Yok" // Varsayılan değer
                }
            }
            "car" -> {
                try {
                    val homeViewModel = ViewModelProvider(requireActivity()).get(com.hanova.ui.home.HomeViewModel::class.java)
                    val parkingStatus = homeViewModel.parkingStatus.value
                    if (!parkingStatus.isNullOrEmpty()) {
                        parkingStatus
                    } else {
                        "Boş" // Varsayılan değer - sensör bağlı değil
                    }
                } catch (e: Exception) {
                    Log.w("MessagesFragment", "Park sensörü verisi alınamadı: ${e.message}")
                    "Boş" // Varsayılan değer
                }
            }
            else -> null
        }}
      private fun getCurrentTime(): String {
        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return format.format(Date())
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Mikrofon izni verildi, sesli tanımayı başlat
                    voiceAssistant.startListening()
                } else {
                    // İzin reddedildi
                    Toast.makeText(
                        requireContext(),
                        "Mikrofon izni gerekli. Sesli komutlar için izin verin.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        if (::voiceAssistant.isInitialized) {
            voiceAssistant.destroy()
        }
        _binding = null
    }
}
