# Complete Guide: Fix Error 28444 "Developer Console is not set up correctly"

## 📊 Your Project Configuration Summary

### ✅ Build Configuration
```
Package Name:     com.gatishil.studyengine
Namespace:        com.gatishil.studyengine
Application ID:   com.gatishil.studyengine
```

### ✅ Your Debug SHA-1 Fingerprint
```
SHA-1: 2C:1F:64:0D:EF:86:A6:BB:98:B7:5C:C1:5F:AD:09:D9:B9:B9:A0:88
```

### ✅ Google OAuth Configuration
```
Web Client ID: 629081030104-8hisk06l39skhmr3v7lqip0d7mh4f00o.apps.googleusercontent.com
```

---

## 🔍 What Is Error 28444?

Error 28444 "DEVELOPER_ERROR" occurs when Google Sign-In cannot verify your app. Common causes:

| Cause | What To Check |
|-------|---------------|
| **Wrong SHA-1** | SHA-1 in Google Console doesn't match your app's SHA-1 |
| **Wrong Package Name** | Package in Google Console doesn't match `com.gatishil.studyengine` |
| **Wrong Client ID** | Using Android Client ID instead of Web Client ID in code |
| **Not Synced** | Google Console changes haven't propagated yet (takes 10-15 min) |
| **Multiple Projects** | Android & Web clients in different Google Cloud projects |

---

## ✅ Step-by-Step Fix

### 1️⃣ VERIFY Your Debug SHA-1 (Already Done ✓)

Your SHA-1 from `gradlew signingReport`:
```
2C:1F:64:0D:EF:86:A6:BB:98:B7:5C:C1:5F:AD:09:D9:B9:B9:A0:88
```

Store location: `C:\Users\BS311\.android\debug.keystore`

### 2️⃣ OPEN Google Cloud Console

1. Go to [console.cloud.google.com](https://console.cloud.google.com/)
2. Make sure you're logged in with the correct Google account
3. **Select your project** from the dropdown (top left)
   - The project ID should start with `629081030104...`

### 3️⃣ VERIFY Android Client ID

1. Click **APIs & Services** → **Credentials** (left sidebar)
2. Look for an **OAuth 2.0 Client ID** with type **Android**
3. **Click it to open details** and verify:

   | Field | Your Value | Screenshot |
   |-------|-----------|-----------|
   | **Client ID** | (doesn't matter for this error) | |
   | **Package Name** | `com.gatishil.studyengine` | ✅ MUST match exactly |
   | **SHA-1** | `2C:1F:64:0D:EF:86:A6:BB:98:B7:5C:C1:5F:AD:09:D9:B9:B9:A0:88` | ✅ MUST match exactly |

4. **If mismatch found**: Click **Edit** and update to correct values
5. **If none exists**: Click **+ CREATE CREDENTIALS** → **OAuth client ID** → **Android** and create one

### 4️⃣ VERIFY Web Client ID

1. Still in **APIs & Services** → **Credentials**
2. Look for **OAuth 2.0 Client ID** with type **Web application**
3. **Copy the full Client ID**, should match what's in your build.gradle.kts:
   ```
   629081030104-8hisk06l39skhmr3v7lqip0d7mh4f00o.apps.googleusercontent.com
   ```

4. **If none exists**: Create one:
   - Click **+ CREATE CREDENTIALS** → **OAuth client ID** → **Web application**
   - Name: `StudyEngine Web Client`
   - Click **Create**
   - Copy the Client ID
   - Update your build.gradle.kts if different

### 5️⃣ ENSURE Google Sign-In API is ENABLED

1. Click **APIs & Services** → **Library** (left sidebar)
2. Search for "Google Sign-In"
3. Click result and verify **ENABLED** button is shown
4. If says "ENABLE", click it to enable

### 6️⃣ REBUILD Your App

```powershell
cd E:\StudyEngine

# Clean build artifacts
.\gradlew clean

# Rebuild
.\gradlew assembleDebug

# Optional: Install on device/emulator
.\gradlew installDebug
```

### 7️⃣ TEST Sign-In

1. Run your app
2. Click "Sign In with Google"
3. You should see:
   - ✅ Google account picker dialog
   - ✅ NO error 28444

---

## 🛠️ What We've Done to Your Code

### ✅ Enhanced Error Messages

Your `AuthViewModel.kt` now provides detailed error messages:

```kotlin
catch (e: Exception) {
    val errorMessage = when {
        e.message?.contains("DEVELOPER_ERROR") == true || 
        e.message?.contains("28444") == true -> {
            "Developer console setup error. Check:\n" +
            "1. SHA-1 fingerprint registered in Google Cloud Console\n" +
            "2. Package name is 'com.gatishil.studyengine'\n" +
            "3. Using Web Client ID (not Android Client ID)\n" +
            "Error: ${e.message}"
        }
        e.message?.contains("USER_CANCELLED") == true -> {
            "Sign-in cancelled"
        }
        e.message?.contains("NETWORK_ERROR") == true -> {
            "Network error. Check your internet connection and backend server."
        }
        else -> e.message ?: "Sign in failed"
    }
    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
    _events.emit(AuthEvent.ShowError(errorMessage))
}
```

Now when error 28444 occurs, your user will see helpful debugging info.

---

## 🔄 Configuration Checklist

Before you test, verify all these match:

### In Your Code:
- [ ] `applicationId = "com.gatishil.studyengine"` in build.gradle.kts
- [ ] `buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "629081030104-8hisk06l39skhmr3v7lqip0d7mh4f00o.apps.googleusercontent.com")`
- [ ] `.setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)` in AuthViewModel

### In Google Cloud Console:
- [ ] Android OAuth Client has Package: `com.gatishil.studyengine`
- [ ] Android OAuth Client has SHA-1: `2C:1F:64:0D:EF:86:A6:BB:98:B7:5C:C1:5F:AD:09:D9:B9:B9:A0:88`
- [ ] Web OAuth Client exists with ID: `629081030104-8hisk06l39skhmr3v7lqip0d7mh4f00o.apps.googleusercontent.com`
- [ ] Google Sign-In API is **ENABLED**
- [ ] Both Android and Web clients are in the **SAME project**

---

## ⏱️ Common Issues & Fixes

### Issue: Still getting error 28444 after updates

**Solutions:**
1. Wait 15 minutes for Google Console to propagate changes
2. Uninstall app: `adb uninstall com.gatishil.studyengine`
3. Clean build: `.\gradlew clean`
4. Rebuild: `.\gradlew assembleDebug`
5. Reinstall: `.\gradlew installDebug`

### Issue: "Google Play Services are not available"

**Solutions:**
1. Install Google Play Services on device/emulator
2. Use a physical device (emulator sometimes has issues)
3. Ensure internet connection works

### Issue: Google account picker doesn't appear

**Solutions:**
1. Add account to device: Settings → Accounts → Add Google Account
2. Check `.setFilterByAuthorizedAccounts(false)` (should allow any account)
3. Verify internet connection

### Issue: "Invalid client" error

**Solutions:**
1. Verify Web Client ID is from SAME project as Android Client
2. Don't use Android Client ID in `setServerClientId()` - must be Web Client ID
3. Verify Client ID format: `XXXXXXXX-XXXXXXXXXXXXXX.apps.googleusercontent.com`

---

## 📱 Testing on Device vs Emulator

### Physical Device:
- ✅ Google Play Services pre-installed
- ✅ Can connect to WiFi with auth
- ✅ Recommended for sign-in testing

### Emulator:
- ⚠️ May need Google Play Services installed
- ⚠️ May have network connectivity issues
- ⚠️ Sometimes caches old credentials

**Recommendation**: Test on a physical device first.

---

## 🔐 Security Notes

- ✅ Web Client ID is safe to include in app (it's how Google Sign-In works)
- ✅ No additional keys needed for basic sign-in
- ✅ Tokens are validated by Google servers, not your client code
- ✅ Android Client SHA-1 is public (derived from your APK)

---

## 📞 Still Having Issues?

1. **Open your app's Logcat** (Android Studio → Logcat tab)
2. Search for "DEVELOPER_ERROR" or "28444"
3. Look for messages like:
   ```
   GoogleSignInError: DEVELOPER_ERROR (wrong SHA-1, package name or time)
   ```
4. Copy the full error message - it often hints at the problem
5. Verify the specific field it mentions against Google Console

---

## 🚀 Next Steps

1. ✅ Open Google Cloud Console
2. ✅ Verify/update Android Client SHA-1
3. ✅ Verify Web Client ID exists
4. ✅ Wait 15 minutes (or rebuild app)
5. ✅ Run `.\gradlew clean ; .\gradlew assembleDebug`
6. ✅ Test signing in

If you get a detailed error message now, it will be much clearer thanks to the enhanced error handling we added!

