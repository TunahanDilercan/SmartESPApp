# SmartESP Sesli Komut Rehberi

Bu rehber, SmartESP uygulamasında gerçek sesli tanıma (Speech Recognition) özelliklerini açıklar.

## 🎤 Sesli Tanıma Özellikleri

### ✅ Yeni Eklenen Özellikler:
1. **Gerçek Android SpeechRecognizer API** entegrasyonu
2. **Türkçe dil desteği** (`tr-TR`)
3. **Canlı ses seviyesi** takibi
4. **Kısmi sonuç** gösterimi
5. **Hata yönetimi** ve geri bildirimi
6. **Test modu** - sesli tanıma yoksa otomatik fallback

## 🗣️ Desteklenen Sesli Komutlar

### 🌡️ Sıcaklık Kontrol Komutları:
```
✅ "Sıcaklığı artır"
✅ "Sıcaklık artır" 
✅ "Isıtma aç"
✅ "Sıcak yap"
✅ "Temperature increase" (İngilizce)
✅ "Heat up" (İngilizce)
```
**Sonuç:** 🔴 Kırmızı LED açılır (ESP8266 `/led?state=1`)

### 🧊 Soğutma Kontrol Komutları:
```
✅ "Sıcaklığı azalt"
✅ "Sıcaklık azalt"
✅ "Soğutma aç"
✅ "Soğuk yap"
✅ "Temperature decrease" (İngilizce)
✅ "Cool down" (İngilizce)
```
**Sonuç:** 🔵 Mavi LED açılır (ESP8266 `/led?state=2`)

### 📊 Sensör Veri Sorguları:
```
✅ "Hava kaç derece?"
✅ "Sıcaklık nedir?"
✅ "Nem oranı nedir?"
✅ "Gaz durumu nedir?"
✅ "Hareket var mı?"
✅ "Park durumu nedir?"
✅ "Araba park edildi mi?"
```

## 🔧 Teknik Detaylar

### SpeechRecognizer Konfigürasyonu:
```kotlin
val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR") // Türkçe
    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
}
```

### Sesli Tanıma Durumları:
1. **🎤 Hazır:** "Konuşmaya hazır"
2. **🎤 Dinliyor:** "Konuşma algılandı..."
3. **🎤 İşliyor:** "Konuşma bitti, işleniyor..."
4. **✅ Başarılı:** Komut tanındı ve işlendi
5. **❌ Hata:** Çeşitli hata mesajları

## 🛡️ Hata Yönetimi

### Sesli Tanıma Hataları:
- **ERROR_AUDIO:** "Ses kayıt hatası"
- **ERROR_NO_MATCH:** "Konuşma anlaşılamadı, tekrar deneyin"
- **ERROR_NETWORK:** "Ağ hatası"
- **ERROR_INSUFFICIENT_PERMISSIONS:** "Mikrofon izni gerekli"
- **ERROR_SPEECH_TIMEOUT:** "Konuşma zaman aşımı"

### Otomatik Fallback:
```kotlin
if (!SpeechRecognizer.isRecognitionAvailable(context)) {
    onCommandReceived("❌ Sesli tanıma bu cihazda desteklenmiyor - Test modu kullanılıyor")
    startTestListening() // Test komutlarına geç
}
```

## 🔐 İzinler

### AndroidManifest.xml'de gerekli izinler:
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
```

### Runtime İzin Kontrolü:
```kotlin
if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) 
    != PackageManager.PERMISSION_GRANTED) {
    // İzin iste
}
```

## 🧪 Test Modu

### Test Modunu Etkinleştirme:
```kotlin
voiceAssistant.setTestMode(true) // Simüle edilmiş komutlar
voiceAssistant.setTestMode(false) // Gerçek sesli tanıma
```

### Test Komutları:
```kotlin
val testCommands = listOf(
    "hava kaç derece",
    "sıcaklığı artır", 
    "sıcaklığı azalt",
    "nem oranı nedir",
    "gaz durumu nedir",
    "hareket var mı",
    "park durumu nedir"
)
```

## 🎯 Kullanım Adımları

### 1. Mikrofon İzni Ver:
- Uygulama açıldığında mikrofon izni istenir
- İzin verilmezse test modu otomatik aktif olur

### 2. Sesli Komut Ver:
- 🎤 Mikrofon butonuna bas
- "Dinliyorum..." mesajını bekle
- Net bir şekilde komut söyle
- Komutun tanınmasını bekle

### 3. Sonucu Gör:
- Tanınan komut chat'te görünür
- ESP8266'ya gerekli istek gönderilir
- LED kontrol veya sensör verisi alınır

## 📱 Desteklenen Cihazlar

### ✅ Çalışır:
- Google Play Services olan Android cihazlar
- Mikrofonu bulunan cihazlar
- Android 6.0+ (API 23+)

### ⚠️ Sınırlı Destek:
- Google Play Services olmayan cihazlar → Test modu
- Mikrofonsuz cihazlar → Sadece yazılı komutlar
- Eski Android sürümleri → Temel özellikler

## 🔊 Sesli Komut Örnekleri

### Örnek Konuşma 1:
```
👤 "Sıcaklığı artır"
🤖 🔴 "Sıcaklık artırılıyor... Kırmızı LED açık!"
🤖 ✅ "Red LED is ON, Blue LED is OFF!"
```

### Örnek Konuşma 2:
```
👤 "Hava kaç derece?"
🤖 🌡️ "Şu anki sıcaklık: 24°C"
```

### Örnek Konuşma 3:
```
👤 "Nem oranı nedir?"
🤖 💧 "Şu anki nem: %65"
```

## 🐛 Sorun Giderme

### Sesli tanıma çalışmıyor:
1. Mikrofon izni kontrolü
2. İnternet bağlantısı kontrolü
3. Google Play Services güncelliği
4. Test modunu dene

### Komutlar tanınmıyor:
1. Net konuş
2. Türkçe telaffuz kullan
3. Gürültülü ortamdan uzak dur
4. Komutları basit tut

### ESP8266 bağlantı hatası:
1. WiFi bağlantısı kontrol et
2. ESP8266 IP adresini doğrula (192.168.4.1)
3. ESP8266'nın açık olduğunu kontrol et

## 🎉 Sonuç

SmartESP artık **gerçek sesli komut desteği** ile çalışıyor! Mikrofon butonuna basın, komutunuzu söyleyin ve IoT cihazlarınızı sesle kontrol edin.
