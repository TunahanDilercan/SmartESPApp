# ESP8266 Kısmi Sensör Bağlantısı Test Rehberi

Bu rehber, ESP8266'da sadece sıcaklık sensörü (DHT11) bağlı olduğunda diğer sensörlerin verilerinin nasıl ele alındığını gösterir.

## Mevcut Durum

### ESP8266 Kod Analizi
ESP8266 kodunda şu endpointler var:
- `/data` - DHT11 (sıcaklık + nem) + MQ2 durumu
- `/pir` - PIR hareket sensörü
- `/piezo` - Piezo park sensörü  
- `/mq2` - MQ2 gaz sensörü (ayrı endpoint)
- `/led` - LED kontrol

### Android Uygulama Hata İşleme

**SensorRepository.kt'de mevcut güvenlik önlemleri:**

1. **Önbellek Sistemi (Cache):**
   - `cachedTemperature`, `cachedHumidity`, `cachedGasStatus` vb.
   - Bağlantı hatası durumunda önbellek verilerini kullanır

2. **Hata Toleransı:**
   - `consecutiveFailures` sayacı ile ardışık hata takibi
   - `maxConsecutiveFailures = 3` limit
   - Hata durumunda önbellek veriler kullanılır

3. **Varsayılan Değerler:**
   ```kotlin
   MessagesFragment.kt'de:
   - Sıcaklık: "22" (varsayılan)
   - Nem: "60" (varsayılan)  
   - Gaz: "Normal" (varsayılan)
   - Hareket: "Hareket Yok" (varsayılan)
   - Park: "Boş" (varsayılan)
   ```

## Test Senaryoları

### Senaryo 1: Sadece DHT11 Bağlı
- ✅ `/data` endpoint çalışır (sıcaklık + nem)
- ❌ `/pir` endpoint hata verir → Önbellek/varsayılan değer
- ❌ `/piezo` endpoint hata verir → Önbellek/varsayılan değer
- ❌ `/mq2` endpoint hata verir → Önbellek/varsayılan değer

### Senaryo 2: DHT11 Sensörü Bozuk
- ❌ `/data` endpoint `{"error":"DHT sensor error"}` döner
- Android uygulama varsayılan değerleri kullanır

## Önerilen İyileştirmeler

### 1. ESP8266 Tarafında İyileştirmeler

Sensör bağlantı durumunu kontrol eden fonksiyonlar eklenebilir:

```cpp
// Her sensör için bağlantı kontrolü
bool isDhtConnected() {
  float testTemp = dht.readTemperature();
  return !isnan(testTemp);
}

bool isPirConnected() {
  // PIR sensör pin durumunu kontrol et
  return digitalRead(pirSensorPin) != -1;
}
```

### 2. Android Tarafında İyileştirmeler

Daha akıllı hata işleme mekanizmaları:

```kotlin
// Sensör durumu bildirimi
data class SensorStatus(
    val isTemperatureAvailable: Boolean,
    val isHumidityAvailable: Boolean, 
    val isMotionAvailable: Boolean,
    val isParkingAvailable: Boolean,
    val isGasAvailable: Boolean
)
```

## Mevcut Durum Değerlendirmesi

**✅ Güvenli:** Uygulama çökmez çünkü:
1. Try-catch blokları var
2. Varsayılan değerler tanımlı
3. Önbellek sistemi çalışıyor
4. Null kontrolleri mevcut

**❌ İyileştirilebilir:**
1. Kullanıcıya hangi sensörlerin çalıştığı bildirilmiyor
2. Hata mesajları genel
3. Sensör durumu göstergesi yok

## Test Komutları

ESP8266'yı test etmek için:

```bash
# Sadece sıcaklık verisi
curl http://192.168.4.1/data

# Hareket sensörü (bağlı değilse timeout)
curl http://192.168.4.1/pir

# Park sensörü (bağlı değilse timeout)  
curl http://192.168.4.1/piezo

# Gaz sensörü (bağlı değilse timeout)
curl http://192.168.4.1/mq2
```

## Sonuç

Mevcut sistem **kısmi sensör bağlantısına dayanıklıdır** ve uygulama çökmez. Varsayılan değerler ve önbellek sistemi sayesinde kullanıcı deneyimi korunur.
