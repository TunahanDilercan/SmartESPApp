# ESP8266 Access Point Konfigürasyonu - SmartESP Uygulaması

## 📡 ESP8266 Access Point Modu

ESP8266 cihazınız **Access Point (AP)** modunda çalışıyor ve sabit IP adresi kullanıyor:

### 🌐 Ağ Bilgileri
- **IP Adresi**: `192.168.4.1`
- **Wi-Fi Ağ Adı**: SmartHomeSensor (veya yapılandırdığınız ad)
- **Mod**: Access Point (AP)

## 📱 Uygulama Konfigürasyonu

### ✅ Güncellenmiş IP Ayarları

**VoiceAssistant.kt** dosyasında IP adresi güncellendi:
```kotlin
private val ESP8266_IP = "192.168.4.1" // ESP8266 Access Point IP adresi
```

### 🔗 HTTP Endpoint'leri

Uygulama şu URL'lere istek gönderecek:

#### Sıcaklık Artırma (Kırmızı LED)
```
http://192.168.4.1/led?state=1
```

#### Sıcaklık Azaltma (Mavi LED)
```
http://192.168.4.1/led?state=2
```

## 🔧 Test Öncesi Gerekli Adımlar

### 1. Wi-Fi Bağlantısı
Android cihazınızı ESP8266'nın Wi-Fi ağına bağlayın:
- **Ayarlar** > **Wi-Fi**
- **SmartHomeSensor** ağını seçin
- Şifreyi girin (ESP8266 kodunuzda tanımladığınız)

### 2. Bağlantı Kontrolü
Terminal/Command Prompt'ta test edin:
```bash
ping 192.168.4.1
```

### 3. Manuel HTTP Test
Web tarayıcısından test edin:
```
http://192.168.4.1/led?state=1  # Kırmızı LED
http://192.168.4.1/led?state=2  # Mavi LED
```

## 🎤 Sesli Komutlar

### Sıcaklık Artırma Komutları (Kırmızı LED)
- "sıcaklığı artır"
- "ısıtma aç"
- "sıcak yap"
- "temperature increase"
- "heat up"

### Sıcaklık Azaltma Komutları (Mavi LED)
- "sıcaklığı azalt"
- "soğutma aç"
- "soğuk yap"
- "temperature decrease"
- "cool down"

## 🚀 Uygulama Testi

1. **ESP8266'yı Başlatın**: Access Point modunda çalıştığından emin olun
2. **Wi-Fi Bağlantısı**: Android cihazı ESP8266 ağına bağlayın
3. **Uygulamayı Başlatın**: SmartESP uygulamasını açın
4. **Messages Sekmesi**: Messages sekmesine gidin
5. **Mikrofon Butonu**: Mikrofon butonuna basın
6. **Sesli Komut**: Sıcaklık komutlarından birini söyleyin
7. **LED Kontrolü**: ESP8266'daki LED'lerin durumunu gözlemleyin

## 📋 Beklenen Sonuçlar

### Başarılı Komut Örnekleri
- 🎤 "sıcaklığı artır" → 🔴 Sıcaklık artırılıyor... Kırmızı LED açık!
- 🎤 "sıcaklığı azalt" → 🔵 Sıcaklık azaltılıyor... Mavi LED açık!
- ✅ Red LED is ON, Blue LED is OFF! (ESP8266 yanıtı)

### Hata Durumları
- ❌ ESP8266 bağlantı hatası: Connection refused
- ❌ LED komutu başarısız: HTTP 404

## 🔍 Sorun Giderme

### Wi-Fi Bağlantı Sorunları
1. ESP8266'nın Access Point modunda çalıştığından emin olun
2. Wi-Fi ağ adı ve şifresini kontrol edin
3. Android cihazın doğru ağa bağlı olduğunu doğrulayın

### HTTP İstek Sorunları
1. IP adresinin doğru olduğunu kontrol edin (192.168.4.1)
2. ESP8266'nın HTTP sunucusunun çalıştığından emin olun
3. Güvenlik duvarı/proxy ayarlarını kontrol edin

### LED Yanıt Sorunları
1. ESP8266 pin bağlantılarını kontrol edin
2. LED'lerin çalışır durumda olduğunu doğrulayın
3. ESP8266 kodundaki pin tanımlarını gözden geçirin

## 📝 Notlar

- ESP8266 Access Point modu sınırlı sayıda cihaza (genellikle 4-8) bağlantı sağlar
- Menzil Access Point modunda daha sınırlıdır
- Güç kesintisi durumunda ESP8266 yeniden başlatılmalıdır
- IP adresi sabit olduğu için ek konfigürasyon gerekmez
