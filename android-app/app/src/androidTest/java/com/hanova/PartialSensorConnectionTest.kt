package com.hanova.test

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hanova.data.SensorRepository
import com.hanova.voice.VoiceAssistant
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Kısmi sensör bağlantısı test senaryoları
 * ESP8266'da sadece DHT11 (sıcaklık) sensörü bağlı olduğunda 
 * uygulamanın nasıl davrandığını test eder
 */
@RunWith(AndroidJUnit4::class)
class PartialSensorConnectionTest {

    private lateinit var context: Context
    private lateinit var voiceAssistant: VoiceAssistant
    private var lastResponse: String = ""
    private var testSensorData = mutableMapOf<String, String?>()    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Test verileri - sadece sıcaklık mevcut, diğerleri null/varsayılan
        testSensorData["temperature"] = "24"  // DHT11 çalışıyor
        testSensorData["humidity"] = "60"     // DHT11 nem kısmı varsayılan
        testSensorData["gas"] = "Normal"      // MQ2 bağlı değil - varsayılan
        testSensorData["motion"] = "Hareket Yok"  // PIR bağlı değil - varsayılan
        testSensorData["car"] = "Boş"         // Piezo bağlı değil - varsayılan
        
        voiceAssistant = VoiceAssistant(
            context = context,
            onCommandReceived = { response ->
                lastResponse = response
            },
            onSensorDataRequest = { sensorType ->
                testSensorData[sensorType]
            }
        )
    }

    @Test
    fun testTemperatureSensorWorking() {
        // Gerçek sıcaklık verisi mevcut
        testSensorData["temperature"] = "26"
        
        voiceAssistant.processVoiceCommand("sıcaklık kaç derece")
        
        // Bekle ve kontrol et
        Thread.sleep(1500)
        assertTrue("Sıcaklık verisi doğru gösterilmeli", 
            lastResponse.contains("26°C") && !lastResponse.contains("kontrol edilsin"))
    }

    @Test
    fun testTemperatureSensorDefault() {
        // Varsayılan sıcaklık verisi (sensör bağlı değil)
        testSensorData["temperature"] = "22"
        
        voiceAssistant.processVoiceCommand("sıcaklık kaç derece")
        
        Thread.sleep(1500)
        assertTrue("Varsayılan sıcaklık uyarısı gösterilmeli", 
            lastResponse.contains("22°C") && lastResponse.contains("kontrol edilsin"))
    }

    @Test
    fun testHumiditySensorDefault() {
        // Varsayılan nem verisi (sensör bağlı değil)
        testSensorData["humidity"] = "60"
        
        voiceAssistant.processVoiceCommand("nem oranı nedir")
        
        Thread.sleep(1500)
        assertTrue("Varsayılan nem uyarısı gösterilmeli", 
            lastResponse.contains("%60") && lastResponse.contains("kontrol edilsin"))
    }

    @Test
    fun testGasSensorDefault() {
        // Varsayılan gaz durumu (MQ2 bağlı değil)
        testSensorData["gas"] = "Normal"
        
        voiceAssistant.processVoiceCommand("gaz durumu nedir")
        
        Thread.sleep(1500)
        assertTrue("Varsayılan gaz uyarısı gösterilmeli", 
            lastResponse.contains("Normal") && lastResponse.contains("kontrol edilsin"))
    }

    @Test
    fun testMotionSensorDefault() {
        // Varsayılan hareket durumu (PIR bağlı değil)
        testSensorData["motion"] = "Hareket Yok"
        
        voiceAssistant.processVoiceCommand("hareket var mı")
        
        Thread.sleep(1500)
        assertTrue("Varsayılan hareket uyarısı gösterilmeli", 
            lastResponse.contains("Hareket Yok") && lastResponse.contains("kontrol edilsin"))
    }

    @Test
    fun testParkingSensorDefault() {
        // Varsayılan park durumu (Piezo bağlı değil)
        testSensorData["car"] = "Boş"
        
        voiceAssistant.processVoiceCommand("park durumu nedir")
        
        Thread.sleep(1500)
        assertTrue("Varsayılan park uyarısı gösterilmeli", 
            lastResponse.contains("Boş") && lastResponse.contains("kontrol edilsin"))
    }

    @Test
    fun testLedControlWorksWithPartialSensors() {
        // LED kontrol kısmi sensör bağlantısında da çalışmalı
        voiceAssistant.processVoiceCommand("sıcaklığı artır")
        
        Thread.sleep(2000) // LED komutu için daha uzun bekle
        assertTrue("LED kontrolü çalışmalı", 
            lastResponse.contains("Sıcaklık artırılıyor") || lastResponse.contains("LED"))
    }

    @Test
    fun testApplicationDoesNotCrash() {
        // Tüm sensör tiplerini sor - uygulama çökmemeli
        val commands = listOf(
            "sıcaklık kaç derece",
            "nem oranı nedir", 
            "gaz var mı",
            "hareket algılandı mı",
            "araba park edildi mi",
            "sıcaklığı artır",
            "sıcaklığı azalt"
        )
        
        commands.forEach { command ->
            try {
                voiceAssistant.processVoiceCommand(command)
                Thread.sleep(1500)
                // Her komut için bir yanıt alınmalı
                assertFalse("Yanıt boş olmamalı", lastResponse.isEmpty())
            } catch (e: Exception) {
                fail("Komut işlenirken hata oluştu: ${command} - ${e.message}")
            }
        }
    }
}
