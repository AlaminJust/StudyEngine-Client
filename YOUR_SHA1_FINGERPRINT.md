# Your Debug SHA-1 Fingerprint

## ✅ Your Exact Debug SHA-1:
```
2C:1F:64:0D:EF:86:A6:BB:98:B7:5C:C1:5F:AD:09:D9:B9:B9:A0:88
```

## Your Other Fingerprints:
- **MD5**: `8A:B2:BB:8E:69:2D:C1:9F:D9:C2:B6:10:B8:62:E2:AC`
- **SHA-256**: `26:BD:2B:0C:74:93:81:B2:28:B3:85:2A:D7:76:B9:7B:56:AC:3F:3D:CA:AD:39:8E:7C:04:16:12:3D:52:3F:B1`

## Keystore Location:
- **Path**: `C:\Users\BS311\.android\debug.keystore`
- **Alias**: `AndroidDebugKey`
- **Valid Until**: Wednesday, October 16, 2052

---

## ✅ REQUIRED ACTION: Update Google Cloud Console

### Go to Google Cloud Console and verify/update:

1. **Go to**: [Google Cloud Console](https://console.cloud.google.com/)
2. **Select your project** (the one with ID starting with `629081030104...`)
3. **Navigate to**: APIs & Services → Credentials
4. **Find your Android Client ID** (or create one if missing)
5. **Update/Verify these fields**:
   - **Package name**: `com.gatishil.studyengine`
   - **SHA-1 certificate fingerprint**: `2C:1F:64:0D:EF:86:A6:BB:98:B7:5C:C1:5F:AD:09:D9:B9:B9:A0:88`
   
6. **Make sure you also have a Web Client ID**:
   - Should look like: `629081030104-8hisk06l39skhmr3v7lqip0d7mh4f00o.apps.googleusercontent.com`
   - This is what's in your `build.gradle.kts`

---

## ✅ Verify Your build.gradle.kts

Your current configuration:
```kotlin
applicationId = "com.gatishil.studyengine"
buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", 
    "\"629081030104-8hisk06l39skhmr3v7lqip0d7mh4f00o.apps.googleusercontent.com\"")
```

✅ **Package name**: Correct (`com.gatishil.studyengine`)
✅ **Web Client ID**: In use correctly via `BuildConfig.GOOGLE_WEB_CLIENT_ID`

---

## ✅ What You Need to Do RIGHT NOW

1. **Copy your SHA-1**: `2C:1F:64:0D:EF:86:A6:BB:98:B7:5C:C1:5F:AD:09:D9:B9:B9:A0:88`

2. **Open Google Cloud Console**:
   - Go to [console.cloud.google.com](https://console.cloud.google.com/)
   - Select your project
   - Go to **APIs & Services** → **Credentials**

3. **Update Android Client ID** (or create if missing):
   - Click **+ CREATE CREDENTIALS** → **OAuth client ID** → **Android**
   - **Name**: `StudyEngine Debug`
   - **Package name**: `com.gatishil.studyengine`
   - **SHA-1 certificate fingerprint**: `2C:1F:64:0D:EF:86:A6:BB:98:B7:5C:C1:5F:AD:09:D9:B9:B9:A0:88`
   - Click **Create**

4. **Verify Web Client ID exists**:
   - Should see `629081030104-8hisk06l39skhmr3v7lqip0d7mh4f00o.apps.googleusercontent.com`
   - If not, create one: **+ CREATE CREDENTIALS** → **OAuth client ID** → **Web application**

5. **In Android Studio**:
   ```powershell
   cd E:\StudyEngine
   .\gradlew clean
   .\gradlew assembleDebug
   ```

6. **Test Sign-In**:
   - Run the app on your device/emulator
   - Click "Sign In with Google"
   - The Google account picker should appear (no error 28444)

---

## Why Error 28444 Happens

Error 28444 occurs when:
- ❌ SHA-1 in Google Console doesn't match actual app SHA-1
- ❌ Package name in Google Console doesn't match app's package name
- ❌ Using wrong Client ID format
- ❌ Credentials not yet synced to Google servers (can take 10-15 minutes)

Your app's actual values:
- **Package**: `com.gatishil.studyengine` ✅
- **SHA-1**: `2C:1F:64:0D:EF:86:A6:BB:98:B7:5C:C1:5F:AD:09:D9:B9:B9:A0:88` ✅
- **Web Client ID**: `629081030104-8hisk06l39skhmr3v7lqip0d7mh4f00o.apps.googleusercontent.com` ✅

Just make sure Google Console has these exact values.

---

## If Error Still Persists

1. **Wait 15 minutes** for Google Console changes to propagate
2. **Uninstall the app** from device: `adb uninstall com.gatishil.studyengine`
3. **Rebuild**: `.\gradlew clean ; .\gradlew assembleDebug`
4. **Reinstall**: `.\gradlew installDebug`
5. **Test again**

If still failing:
- Verify you're on the **same WiFi** as your Google-connected device
- Check **Google Play Services** is installed/updated on the device
- Ensure **internet connection** is working on the device

