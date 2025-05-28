package com.hanova

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hanova.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d("MainActivity", "onCreate başladı")

            // Tema ve ActionBar ayarları
            supportActionBar?.hide()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            // View Binding güvenli başlatma
            _binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Navigasyon ayarları - basit tutuyoruz
            setupBasicNavigation()

            Log.d("MainActivity", "onCreate tamamlandı")
        } catch (e: Exception) {
            Log.e("MainActivity", "Başlatma hatası: ${e.message}")
            Toast.makeText(this, "Uygulama başlatılamadı: ${e.message}", Toast.LENGTH_LONG).show()
            finish() // Kritik hatalarda uygulamayı kapat
        }
    }

    private fun setupBasicNavigation() {
        try {
            val navView: BottomNavigationView = binding.navView
            val navController = findNavController(R.id.nav_host_fragment_activity_main)

            // Temel navigasyon ayarları
            navView.setupWithNavController(navController)
        } catch (e: Exception) {
            Log.e("MainActivity", "Navigasyon hatası: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
