#!/usr/bin/env python3
"""
SmartESP Voice Recognition Verification Script
Verifies that all components are properly configured for voice recognition
"""

import os
import re
import json

def check_file_exists(file_path, description):
    """Check if a file exists and return status"""
    if os.path.exists(file_path):
        print(f"✅ {description}: Found")
        return True
    else:
        print(f"❌ {description}: Not found at {file_path}")
        return False

def check_file_content(file_path, pattern, description):
    """Check if file contains specific pattern"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
            if re.search(pattern, content):
                print(f"✅ {description}: Configured")
                return True
            else:
                print(f"❌ {description}: Not configured")
                return False
    except Exception as e:
        print(f"❌ {description}: Error reading file - {e}")
        return False

def main():
    print("🎤 SmartESP Voice Recognition Verification")
    print("=" * 50)
    
    base_path = "d:/FilesHan/kod/mobileApp/SmartESP/SmartESPApp/android-app"
    app_path = f"{base_path}/app/src/main"
    
    checks_passed = 0
    total_checks = 0
    
    # Check core files exist
    files_to_check = [
        (f"{app_path}/java/com/hanova/ui/messages/VoiceAssistant.kt", "VoiceAssistant.kt"),
        (f"{app_path}/java/com/hanova/ui/messages/MessagesFragment.kt", "MessagesFragment.kt"),
        (f"{app_path}/AndroidManifest.xml", "AndroidManifest.xml"),
        (f"{base_path}/app/build.gradle.kts", "build.gradle.kts")
    ]
    
    print("\n📁 File Existence Check:")
    for file_path, description in files_to_check:
        if check_file_exists(file_path, description):
            checks_passed += 1
        total_checks += 1
    
    # Check permissions in AndroidManifest.xml
    print("\n🔐 Permission Check:")
    manifest_path = f"{app_path}/AndroidManifest.xml"
    permission_patterns = [
        (r'android\.permission\.RECORD_AUDIO', "Microphone Permission"),
        (r'android\.permission\.INTERNET', "Internet Permission"),
        (r'android\.permission\.MODIFY_AUDIO_SETTINGS', "Audio Settings Permission")
    ]
    
    for pattern, description in permission_patterns:
        if check_file_content(manifest_path, pattern, description):
            checks_passed += 1
        total_checks += 1
    
    # Check VoiceAssistant implementation
    print("\n🎤 Voice Recognition Implementation:")
    voice_assistant_path = f"{app_path}/java/com/hanova/ui/messages/VoiceAssistant.kt"
    voice_patterns = [
        (r'import android\.speech\.SpeechRecognizer', "SpeechRecognizer Import"),
        (r'import android\.speech\.RecognitionListener', "RecognitionListener Import"),
        (r'class VoiceAssistant.*: RecognitionListener', "RecognitionListener Implementation"),
        (r'override fun onResults', "onResults Method"),
        (r'override fun onError', "onError Method"),
        (r'tr-TR', "Turkish Language Support"),
        (r'ESP8266_IP = "192\.168\.4\.1"', "ESP8266 IP Configuration"),
        (r'sendLedCommand', "LED Control Function")
    ]
    
    for pattern, description in voice_patterns:
        if check_file_content(voice_assistant_path, pattern, description):
            checks_passed += 1
        total_checks += 1
    
    # Check MessagesFragment integration
    print("\n📱 MessagesFragment Integration:")
    messages_fragment_path = f"{app_path}/java/com/hanova/ui/messages/MessagesFragment.kt"
    integration_patterns = [
        (r'import com\.hanova\.ui\.messages\.VoiceAssistant', "VoiceAssistant Import"),
        (r'private lateinit var voiceAssistant: VoiceAssistant', "VoiceAssistant Declaration"),
        (r'setupVoiceAssistant\(\)', "VoiceAssistant Setup"),
        (r'voiceAssistant\.startListening\(\)', "Voice Listening Start"),
        (r'voiceAssistant\.destroy\(\)', "VoiceAssistant Cleanup")
    ]
    
    for pattern, description in integration_patterns:
        if check_file_content(messages_fragment_path, pattern, description):
            checks_passed += 1
        total_checks += 1
    
    # Check build configuration
    print("\n🏗️ Build Configuration:")
    build_gradle_path = f"{base_path}/app/build.gradle.kts"
    build_patterns = [
        (r'compileSdk.*\d+', "Compile SDK Version"),
        (r'minSdk.*\d+', "Minimum SDK Version"),
        (r'implementation.*androidx', "AndroidX Dependencies")
    ]
    
    for pattern, description in build_patterns:
        if check_file_content(build_gradle_path, pattern, description):
            checks_passed += 1
        total_checks += 1
    
    # Summary
    print("\n" + "=" * 50)
    print(f"📊 VERIFICATION SUMMARY")
    print(f"Checks Passed: {checks_passed}/{total_checks}")
    percentage = (checks_passed / total_checks) * 100
    print(f"Success Rate: {percentage:.1f}%")
    
    if checks_passed == total_checks:
        print("\n🎉 ALL CHECKS PASSED!")
        print("✅ Voice Recognition is properly configured")
        print("✅ Ready for testing on Android device")
        print("\n🚀 Next Steps:")
        print("1. Build and install app on Android device")
        print("2. Grant microphone permission")
        print("3. Connect to ESP8266 WiFi (192.168.4.1)")
        print("4. Test voice commands in Messages tab")
    elif checks_passed >= total_checks * 0.8:
        print("\n⚠️  MOSTLY CONFIGURED")
        print(f"Minor issues detected ({total_checks - checks_passed} failures)")
        print("Voice recognition should work with minor fixes")
    else:
        print("\n❌ CONFIGURATION ISSUES")
        print(f"Significant issues detected ({total_checks - checks_passed} failures)")
        print("Review the failed checks above")

if __name__ == "__main__":
    main()
