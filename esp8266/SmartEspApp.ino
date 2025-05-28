#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <DHT.h>

// WiFi Settings
const char* ssid = "SmartHomeSensor"; // WiFi SSID
const char* password = "19077028";    // WiFi Password

// Static IP Settings
IPAddress local_IP(192, 168, 4, 1);
IPAddress gateway(192, 168, 4, 1);
IPAddress subnet(255, 255, 255, 0);

// LED Pins
const int redLedPin = D1; // GPIO pin for the red LED sıcaklık arttırma
const int blueLedPin = D2; // GPIO pin for the blue LED sıcaklık azaltma
const int pirLedPin = D8; // GPIO pin for PIR-triggered LED

// DHT Sensor Settings
#define DHTPIN D4 // GPIO pin for DHT data
#define DHTTYPE DHT11 // DHT11 or DHT22
DHT dht(DHTPIN, DHTTYPE);

// MQ2 Sensor Settings
const int mq2DigitalPin = D5; // GPIO pin for MQ2 digital output

// PIR Sensor Settings
const int pirSensorPin = D6; // GPIO pin for PIR motion sensor

// Piezo Sensor Settings
const int piezoPin = A0; // GPIO pin for Piezo sensor
const int piezoThreshold = 15; // Threshold for detecting activity

// Web Server
ESP8266WebServer server(80);

// Timer Variables
unsigned long lastActionTime = 0;
const unsigned long timeout = 15000; // 15 seconds
unsigned long pirLedStartTime = 0;
bool pirLedActive = false;
unsigned long lastDhtReadTime = 0;
const unsigned long dhtReadInterval = 2000; // Read DHT every 2 seconds to prevent errors

// Cache for sensor readings
float cachedTemperature = 0;
float cachedHumidity = 0;
bool lastPirStatus = false;
bool lastMq2Status = false;

// Park Status Variables
bool parkYeriDolu = false; // Initial state of the parking space
unsigned long lastDarbeTime = 0;
const unsigned long debounceDelay = 2000; // 2 seconds debounce delay

void setup() {
  Serial.begin(115200);

  // Configure Static IP
  if (!WiFi.softAPConfig(local_IP, gateway, subnet)) {
    Serial.println("Error setting static IP configuration!");
  }

  // Start Access Point
  WiFi.softAP(ssid, password);
  Serial.println("Access Point started!");
  Serial.print("IP Address: ");
  Serial.println(WiFi.softAPIP());

  // Initialize LED pins
  pinMode(redLedPin, OUTPUT);
  pinMode(blueLedPin, OUTPUT);
  pinMode(pirLedPin, OUTPUT);
  digitalWrite(redLedPin, LOW);
  digitalWrite(blueLedPin, LOW);
  digitalWrite(pirLedPin, LOW);

  // Initialize MQ2 pin
  pinMode(mq2DigitalPin, INPUT);

  // Initialize PIR sensor pin
  pinMode(pirSensorPin, INPUT);

  // Initialize DHT Sensor
  dht.begin();

  // Define root endpoint
  server.on("/", []() {
    server.send(200, "text/plain", "ESP8266 is running successfully!\nUse /led?state=1 to turn on the red LED and off the blue LED.\nUse /led?state=2 to turn on the blue LED and off the red LED.\nUse /data to get temperature and humidity data.\nUse /mq2 to get gas sensor status.\nUse /pir to get motion sensor status.\nUse /piezo to get piezo sensor status.");
  });

  // Define LED control endpoint
  server.on("/led", []() {
    if (server.hasArg("state")) {
      String state = server.arg("state");
      if (state == "1") {
        digitalWrite(redLedPin, HIGH); // Turn red LED on
        digitalWrite(blueLedPin, LOW); // Turn blue LED off
        lastActionTime = millis();
        server.send(200, "text/plain", "Red LED is ON, Blue LED is OFF!");
      } else if (state == "2") {
        digitalWrite(blueLedPin, HIGH); // Turn blue LED on
        digitalWrite(redLedPin, LOW); // Turn red LED off
        lastActionTime = millis();
        server.send(200, "text/plain", "Blue LED is ON, Red LED is OFF!");
      } else {
        server.send(400, "text/plain", "Error: Invalid state parameter.");
      }
    } else {
      server.send(400, "text/plain", "Error: 'state' parameter missing.");
    }
  });

  // Define DHT data endpoint
  server.on("/data", []() {
    // Read DHT sensor with rate limiting to prevent errors
    unsigned long currentMillis = millis();
    if (currentMillis - lastDhtReadTime >= dhtReadInterval) {
      float newTemp = dht.readTemperature();
      float newHumidity = dht.readHumidity();
      
      // Only update cached values if readings are valid
      if (!isnan(newTemp)) {
        cachedTemperature = newTemp;
      }
      if (!isnan(newHumidity)) {
        cachedHumidity = newHumidity;
      }
      
      lastDhtReadTime = currentMillis;
    }

    // Check if we have valid cached data
    if (isnan(cachedTemperature) || isnan(cachedHumidity)) {
      server.send(500, "application/json", "{\"error\":\"DHT sensor error\"}");
      return;
    }

    Serial.print("Temperature: ");
    Serial.print(cachedTemperature);
    Serial.println("°C");
    Serial.print("Humidity: ");
    Serial.print(cachedHumidity);
    Serial.println("%");

    String json = "{\"temperature\":" + String(cachedTemperature, 1) + 
                 ",\"humidity\":" + String(cachedHumidity, 1) + 
                 ",\"status\":\"" + (lastMq2Status ? "Karbondioksit algilandi!" : "Karbondioksit Algilanmadi") + "\"}";
    
    server.send(200, "application/json", json);
  });

  // Define MQ2 data endpoint
  server.on("/mq2", []() {
    int mq2Status = digitalRead(mq2DigitalPin); // Read MQ2 digital output
    String jsonResponse;

    if (mq2Status == HIGH) { // Gas detected
      jsonResponse = "{\"status\":\"alert\",\"message\":\"Karbondioksit algilandi!\"}";
    } else { // No gas detected
      jsonResponse = "{\"status\":\"normal\",\"message\":\"Karbondioksit Algilanmadi\"}";
    }

    Serial.print("MQ2 Status: ");
    Serial.println(mq2Status == HIGH ? "Gas Detected" : "No Gas Detected");

    server.send(200, "application/json", jsonResponse);
  });

  // Define PIR data endpoint
  server.on("/pir", []() {
    int pirStatus = digitalRead(pirSensorPin); // PIR Hareket Sensoru
    String motionStatus;

    if (pirStatus == HIGH) { // Motion detected
      motionStatus = "{\"status\":\"motion_detected\",\"message\":\"Hareket algilandi!\"}";
    } else { // No motion detected
      motionStatus = "{\"status\":\"no_motion\",\"message\":\"Hareket algilanmadi.\"}";
    }

    Serial.print("PIR Status: ");
    Serial.println(pirStatus == HIGH ? "Motion Detected" : "No Motion Detected");

    server.send(200, "application/json", motionStatus);
  });

  // Define Piezo sensor endpoint
  server.on("/piezo", []() {
    int piezoValue = analogRead(piezoPin); // Read piezo sensor value

    // Check if piezo value exceeds threshold and debounce delay has passed
    if (piezoValue > piezoThreshold && (millis() - lastDarbeTime > debounceDelay)) {
      parkYeriDolu = !parkYeriDolu; // Toggle park status
      lastDarbeTime = millis(); // Update last darbe time

      // Update LEDs based on park status
      if (parkYeriDolu) {
        digitalWrite(redLedPin, HIGH);
        digitalWrite(blueLedPin, LOW);
        Serial.println("Park DOLU (Arac girdi)");
      } else {
        digitalWrite(redLedPin, LOW);
        digitalWrite(blueLedPin, HIGH);
        Serial.println("Park BOS (Arac cikti)");
      }
    }

    // Send JSON response with park status
    String jsonResponse = "{";
    jsonResponse += "\"status\":\"" + String(parkYeriDolu ? "occupied" : "empty") + "\",";
    jsonResponse += "\"message\":\"" + String(parkYeriDolu ? "Park yeri dolu" : "Park yeri bos") + "\"}";

    server.send(200, "application/json", jsonResponse);
  });

  server.begin();
  Serial.println("HTTP Server started.");
}

void loop() {
  server.handleClient(); // Handle HTTP requests

  // Read sensors periodically to update cache
  unsigned long currentMillis = millis();
  
  // Read DHT sensor with rate limiting
  if (currentMillis - lastDhtReadTime >= dhtReadInterval) {
    float newTemp = dht.readTemperature();
    float newHumidity = dht.readHumidity();
    
    // Only update cached values if readings are valid
    if (!isnan(newTemp)) {
      cachedTemperature = newTemp;
    }
    if (!isnan(newHumidity)) {
      cachedHumidity = newHumidity;
    }
    
    lastDhtReadTime = currentMillis;
  }
  
  // Update MQ2 sensor cache
  lastMq2Status = digitalRead(mq2DigitalPin) == HIGH;
  
  // Update PIR sensor cache
  lastPirStatus = digitalRead(pirSensorPin) == HIGH;

  // Check for timeout
  if (currentMillis - lastActionTime > timeout) {
    digitalWrite(redLedPin, LOW); // Turn off red LED
    digitalWrite(blueLedPin, LOW); // Turn off blue LED
  }

  // Handle PIR LED logic
  if (lastPirStatus && !pirLedActive) {
    digitalWrite(pirLedPin, HIGH); // Turn on PIR LED
    pirLedStartTime = currentMillis;
    pirLedActive = true;
  }

  if (pirLedActive && currentMillis - pirLedStartTime >= 20000) {
    digitalWrite(pirLedPin, LOW); // Turn off PIR LED after 20 seconds
    pirLedActive = false;
  }
  
  // Read piezo sensor and handle debouncing
  int piezoValue = analogRead(piezoPin);
  if (piezoValue > piezoThreshold && (currentMillis - lastDarbeTime > debounceDelay)) {
    parkYeriDolu = !parkYeriDolu; // Toggle park status
    lastDarbeTime = currentMillis; // Update last darbe time

    // Update LEDs based on park status
    if (parkYeriDolu) {
      digitalWrite(redLedPin, HIGH);
      digitalWrite(blueLedPin, LOW);
      Serial.println("Park DOLU (Arac girdi)");
    } else {
      digitalWrite(redLedPin, LOW);
      digitalWrite(blueLedPin, HIGH);
      Serial.println("Park BOS (Arac cikti)");
    }
  }
  
  // Small delay to prevent processor overload
  delay(10);
}
