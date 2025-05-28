package com.hanova

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hanova.voice.VoiceAssistant
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Instrumented test for VoiceAssistant functionality
 * Tests voice command processing and LED control integration
 */
@RunWith(AndroidJUnit4::class)
class VoiceAssistantTest {

    @Test
    fun testVoiceAssistantBasicFunctionality() {
        // Context of the app under test
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var lastResponse = ""
        val testSensorData = mutableMapOf<String, String?>()
        
        // Test sensor data - simulate DHT11 working
        testSensorData["temperature"] = "24"
        testSensorData["humidity"] = "60"
        testSensorData["gas"] = "Normal"
        testSensorData["motion"] = "Hareket Yok"
        testSensorData["car"] = "Boş"

        // Initialize VoiceAssistant with test callbacks
        val voiceAssistant = VoiceAssistant(
            context = context,
            onCommandReceived = { response: String ->
                lastResponse = response
            },
            onSensorDataRequest = { sensorType: String ->
                testSensorData[sensorType]
            }
        )
        
        // Enable test mode to avoid actual speech recognition
        voiceAssistant.setTestMode(true)
        
        // Test temperature query command
        testSensorData["temperature"] = "26"
        voiceAssistant.processVoiceCommand("sıcaklık kaç derece")
        
        // Wait for async processing
        Thread.sleep(1500)
        
        assertTrue("Temperature response should contain the value", 
            lastResponse.contains("26°C"))
    }

    @Test
    fun testTemperatureControlCommands() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var lastResponse = ""

        val voiceAssistant = VoiceAssistant(
            context = context,
            onCommandReceived = { response: String ->
                lastResponse = response
            },
            onSensorDataRequest = { _: String -> null }
        )
        
        voiceAssistant.setTestMode(true)
        
        // Test temperature increase command (Red LED)
        voiceAssistant.processVoiceCommand("sıcaklığı artır")
        Thread.sleep(2000)
        
        assertTrue("Should respond with red LED activation", 
            lastResponse.contains("🔴") && lastResponse.contains("Sıcaklık artırılıyor"))
            
        // Test temperature decrease command (Blue LED)
        voiceAssistant.processVoiceCommand("sıcaklığı azalt")
        Thread.sleep(2000)
        
        assertTrue("Should respond with blue LED activation", 
            lastResponse.contains("🔵") && lastResponse.contains("Sıcaklık azaltılıyor"))
    }

    @Test
    fun testHumidityQuery() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var lastResponse = ""
        val testSensorData = mutableMapOf<String, String?>()

        val voiceAssistant = VoiceAssistant(
            context = context,
            onCommandReceived = { response: String ->
                lastResponse = response
            },
            onSensorDataRequest = { sensorType: String ->
                testSensorData[sensorType]
            }
        )
        
        voiceAssistant.setTestMode(true)
        
        // Test humidity query command
        testSensorData["humidity"] = "65"
        voiceAssistant.processVoiceCommand("nem oranı nedir")
        Thread.sleep(1500)
        
        assertTrue("Humidity response should contain the value", 
            lastResponse.contains("%65"))
    }
}
}
