package com.hanova.ui.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hanova.R
import com.hanova.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        // Setup button listeners
        binding.button11.setOnClickListener {
            homeViewModel.setLedState(2) { _ ->
                // Sessiz hata işleme: Toast mesajı yerine sadece günlüğe kaydet
                // Kullanıcıya bildirim gösterme
            }
        }
        
        binding.button10.setOnClickListener {
            homeViewModel.setLedState(1) { _ ->
                // Sessiz hata işleme: Toast mesajı yerine sadece günlüğe kaydet
                // Kullanıcıya bildirim gösterme
            }
        }
        
        // Observe LiveData from ViewModel
        homeViewModel.temperature.observe(viewLifecycleOwner) { temperature ->
            binding.tvTemperature.text = "${temperature}°C"
        }

        homeViewModel.humidity.observe(viewLifecycleOwner) { humidity ->
            binding.tvHumidity.text = "${humidity}%"
        }
        
        homeViewModel.gasStatus.observe(viewLifecycleOwner) { status ->
            binding.dumantext.text = status
        }
        
        homeViewModel.motionStatus.observe(viewLifecycleOwner) { status ->
            binding.harekettext.text = status
        }
        
        homeViewModel.parkingStatus.observe(viewLifecycleOwner) { status ->
            binding.otoparktext.text = status
        }
        
        homeViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            // Hata mesajları gösterilmeyecek - sadece konsola yazdır
            System.out.println("Error: $message")
        }
        
        // Observe connection status and update connection indicator
        homeViewModel.connectionStatus.observe(viewLifecycleOwner) { _ ->
            activity?.runOnUiThread {
                // Tüm bağlantı bildirimlerini gizle
                val connectionContainer = activity?.findViewById<View>(R.id.connectionStatusContainer)
                connectionContainer?.visibility = View.GONE
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // PNG simgeleri ayarla
        binding.tempIcon.setImageResource(R.drawable.sunn)
        binding.imgDuman.setImageResource(R.drawable.cloud)
        binding.imgHareket.setImageResource(R.drawable.next)
        binding.imgOtopark.setImageResource(R.drawable.river)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
