package com.hanova.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hanova.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var dashboardViewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            Log.d("DashboardFragment", "onCreateView başladı")
            _binding = FragmentDashboardBinding.inflate(inflater, container, false)
            
            // Initialize ViewModel
            dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
            
            // Setup RecyclerView and Adapter
            setupRecyclerView()
            
            // Setup UI interactions
            setupUIListeners()
            
            // Observe LiveData
            observeData()
            
            return binding.root
        } catch (e: Exception) {
            Log.e("DashboardFragment", "Fragment oluşturma hatası: ${e.message}")
            Toast.makeText(context, "Fragment başlatılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
            // Çökmeyi önlemek için boş bir view dön
            return View(context)
        }
    }

    private fun setupRecyclerView() {
        // chatAdapter = ChatAdapter(emptyList())
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            // adapter = chatAdapter
        }
    }

    private fun setupUIListeners() {
        // Send button click listener
        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                dashboardViewModel.sendUserMessage(message)
                binding.messageEditText.text.clear()
            }
        }

        // Example command chips click listeners
        binding.chip1.setOnClickListener {
            dashboardViewModel.sendUserMessage("Sıcaklık nedir?")
        }
        
        binding.chip2.setOnClickListener {
            dashboardViewModel.sendUserMessage("Işığı aç")
        }
        
        binding.chip3.setOnClickListener {
            dashboardViewModel.sendUserMessage("Nem oranı nedir?")
        }
        
        binding.chip4.setOnClickListener {
            dashboardViewModel.sendUserMessage("Işığı kapat")
        }
        
        binding.chip5.setOnClickListener {
            dashboardViewModel.sendUserMessage("Tüm sensörleri göster")
        }
    }

    private fun observeData() {
        // Observe chat messages
        dashboardViewModel.messages.observe(viewLifecycleOwner) { messages ->
            // chatAdapter.updateMessages(messages)
            // Scroll to bottom when new message arrives
            if (messages.isNotEmpty()) {
                binding.chatRecyclerView.scrollToPosition(messages.size - 1)
            }
        }

        // Observe sensor data and update recent commands display
        dashboardViewModel.temperature.observe(viewLifecycleOwner) { temperature ->
            binding.tvCommand1.text = "GET Sıcaklık: ${temperature}°C"
        }

        dashboardViewModel.humidity.observe(viewLifecycleOwner) { _ ->
            // Could update another command display if needed
        }

        dashboardViewModel.gasStatus.observe(viewLifecycleOwner) { _ ->
            // Could update command display based on gas status
        }

        dashboardViewModel.motionStatus.observe(viewLifecycleOwner) { _ ->
            // Could update command display based on motion status
        }

        dashboardViewModel.parkingStatus.observe(viewLifecycleOwner) { _ ->
            // Could update command display based on parking status
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
