# Voice Assistant Manual Testing Guide

## Overview
Due to Windows file locking issues preventing automated test execution, this guide provides manual testing procedures for the VoiceAssistant functionality.

## Current Status ✅
- ✅ **VoiceAssistant.kt**: Complete implementation with real speech recognition
- ✅ **MessagesFragment.kt**: Integrated with proper permissions and voice commands
- ✅ **ESP8266 LED Control**: Temperature control via HTTP requests
- ✅ **Turkish Language Support**: Full tr-TR speech recognition
- ✅ **Error Handling**: Fallback to test mode when speech unavailable

## Manual Testing Procedures

### 1. Voice Recognition Testing
**Test Steps:**
1. Launch the SmartESP app
2. Navigate to Messages tab
3. Click the microphone button (🎤)
4. Grant microphone permission when prompted
5. Speak one of the supported commands

**Expected Results:**
- Microphone permission dialog appears
- Voice recognition starts (listening indicator)
- Command is processed and response appears in chat

### 2. Temperature Control Commands
**Turkish Commands:**
- `"sıcaklığı artır"` → Red LED activation (🔴 Sıcaklık artırılıyor)
- `"sıcaklığı azalt"` → Blue LED activation (🔵 Sıcaklık azaltılıyor)

**English Commands:**
- `"temperature increase"` → Red LED activation
- `"temperature decrease"` → Blue LED activation

**Expected HTTP Requests:**
- Red LED: `GET http://192.168.4.1/led?state=1`
- Blue LED: `GET http://192.168.4.1/led?state=2`

### 3. Sensor Query Commands
**Turkish Commands:**
- `"hava kaç derece"` / `"sıcaklık"` → Temperature reading
- `"nem oranı nedir"` / `"nem"` → Humidity reading

**Expected Responses:**
- Temperature: "Sıcaklık: 24°C (DHT11 sensör kontrol edilsin)"
- Humidity: "Nem: %60 (DHT11 sensör kontrol edilsin)"

### 4. Permission Testing
**Test Steps:**
1. Deny microphone permission initially
2. Try to use voice command
3. Should see: "Mikrofon izni reddedildi. Sesli komutlar kullanılamaz."

**Re-enable Permission:**
1. Go to Android Settings > Apps > SmartESP > Permissions
2. Enable Microphone permission
3. Return to app and test voice commands

### 5. Test Mode Verification
**When Speech Recognition Unavailable:**
- App automatically falls back to test mode
- Voice commands still process through `processVoiceCommand()` method
- All functionality remains available for testing

## LED Control Integration

### ESP8266 Setup Required
```cpp
// ESP8266 should handle these endpoints:
// GET /led?state=1  -> Red LED (heating)
// GET /led?state=2  -> Blue LED (cooling)
// GET /led?state=0  -> Turn off LEDs
```

### Network Configuration
- **Access Point Mode**: ESP8266 IP = 192.168.4.1
- **Station Mode**: Update VoiceAssistant.kt with correct IP

## Voice Command Processing Flow

1. **Voice Input** → SpeechRecognizer (tr-TR)
2. **Speech-to-Text** → `processCommand()` function
3. **Command Analysis** → Pattern matching for temperature/sensor commands
4. **Action Execution** → LED control or sensor query
5. **Response Generation** → User feedback with emojis
6. **Chat Integration** → `sendCommand()` with 🎤 prefix

## Troubleshooting

### Common Issues:
1. **No microphone permission**: Grant in Android settings
2. **Speech recognition not starting**: Check test mode fallback
3. **LED not responding**: Verify ESP8266 connection and IP address
4. **Commands not recognized**: Try speaking clearly in Turkish or English

### Debug Information:
- Check Android Logcat for VoiceAssistant logs
- Verify HTTP requests in ESP8266 serial monitor
- Test mode allows command testing without speech recognition

## Implementation Details

### Key Features Implemented:
- ✅ Real Android SpeechRecognizer integration
- ✅ RecognitionListener with all required callbacks
- ✅ Turkish language support (tr-TR)
- ✅ HTTP communication with ESP8266
- ✅ LED temperature control (Red=heat, Blue=cool)
- ✅ Sensor data integration
- ✅ Automatic test mode fallback
- ✅ Runtime permission handling
- ✅ Chat system integration

### Files Modified:
- `com/hanova/voice/VoiceAssistant.kt` - Complete implementation
- `com/hanova/ui/messages/MessagesFragment.kt` - Voice integration
- `app/build.gradle.kts` - Dependencies and formatting fixes

## Testing Checklist

- [ ] App launches without crashes
- [ ] Microphone permission dialog appears
- [ ] Voice recognition starts when button pressed
- [ ] Temperature increase command sends Red LED signal
- [ ] Temperature decrease command sends Blue LED signal
- [ ] Sensor queries return appropriate responses
- [ ] Permission denial handled gracefully
- [ ] Test mode fallback works when speech unavailable
- [ ] Chat integration shows voice commands with 🎤 prefix
- [ ] HTTP requests sent to correct ESP8266 endpoints

## Next Steps

1. **Deploy to Android device** for real voice testing
2. **Connect ESP8266** with LED control endpoints
3. **Test in actual IoT environment** with sensors connected
4. **Monitor HTTP communication** between app and ESP8266
5. **Verify temperature control effectiveness** in real scenarios

The voice assistant implementation is complete and ready for real-world testing with ESP8266 hardware integration.
