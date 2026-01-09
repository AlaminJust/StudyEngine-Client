# Error 28444 "Developer Console is not set up correctly" - Fix Guide

## What Causes Error 28444?

This error occurs when Google Sign-In cannot verify that your app is legitimate. It happens due to mismatches between:

1. ❌ SHA-1 fingerprint in code ≠ SHA-1 in Google Cloud Console
2. ❌ Package name in code ≠ Package name in Google Cloud Console
3. ❌ Web Client ID type (using Android Client ID instead of Web Client ID)
4. ❌ Using credentials from wrong Google Cloud project
5. ❌ SHA-1 from release keystore when running debug app (or vice versa)

---

## Your Current Configuration

### ✅ In build.gradle.kts:
```kotlin
applicationId = "com.gatishil.studyengine"  // Package name is correct
buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", 
    "\"629081030104-8hisk06l39skhmr3v7lqip0d7mh4f00o.apps.googleusercontent.com\"")
```

### ✅ In AuthViewModel.kt:
```kotlin
.setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)  // Using Web Client ID correctly
```

---

## Step 1: Verify Your SHA-1 Fingerprint

Since you said you added the debug SHA-1 key, we need to confirm it's correct.

### Get your current SHA-1:

**Using Gradle (Recommended):**
```powershell
cd E:\StudyEngine
.\gradlew signingReport
```

Look for output like:
```
Variant: debug
Config: debug
Store: C:\Users\YourName\.android\debug.keystore
SHA1: AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD
```

**Copy the SHA-1 value (with colons).**

---

## Step 2: Verify Google Cloud Console Setup

Go to [Google Cloud Console](https://console.cloud.google.com/) and check:

### A. Correct Project Selected
- Top left corner shows your project name
- It should match the project where you created the OAuth credentials

### B. Android OAuth Client ID
1. Navigate to **APIs & Services** → **Credentials**
2. Find your **Android** Client ID
3. Click it to open details
4. Verify:
   - ✅ **Package name**: `com.gatishil.studyengine`
   - ✅ **SHA-1 certificate fingerprint**: Matches your debug SHA-1 (from Step 1)
   - ✅ **Fingerprint format**: Must have colons (AA:BB:CC:DD... not AABBCCDD...)

### C. Web OAuth Client ID
1. Still in **Credentials** page
2. Find your **Web application** Client ID
3. This is the one you're using in build.gradle.kts
4. **Copy the full Client ID**: `629081030104-8hisk06l39skhmr3v7lqip0d7mh4f00o.apps.googleusercontent.com`

### D. Authorized Redirect URIs (Web Client)
For the Web Client, make sure you have these authorized URIs:
```
http://localhost:8080/
https://localhost/
```
(This is for token verification. The format `XXXXXXXX.apps.googleusercontent.com` is primary.)

---

## Step 3: Update Google Cloud Console (If Needed)

If you find mismatches:

### To Update Android Client SHA-1:
1. Go to **Credentials** → Click your Android Client ID
2. Click **Edit**
3. Update the SHA-1 field with your correct debug SHA-1
4. Click **Save**

### To Create New Android Client (If none exists):
1. Click **+ CREATE CREDENTIALS** → **OAuth client ID**
2. Select **Android**
3. Enter:
   - **Name**: `StudyEngine Debug`
   - **Package name**: `com.gatishil.studyengine`
   - **SHA-1 certificate fingerprint**: Your debug SHA-1 (from Step 1)
4. Click **Create**

---

## Step 4: Verify Your build.gradle.kts

Your current configuration looks correct:

```kotlin
android {
    namespace = "com.gatishil.studyengine"
    
    defaultConfig {
        applicationId = "com.gatishil.studyengine"  // ✅ Correct
        // ...
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", 
            "\"629081030104-8hisk06l39skhmr3v7lqip0d7mh4f00o.apps.googleusercontent.com\"")
    }
}
```

**However**, let me verify this Web Client ID matches your project:

The Client ID `629081030104-8hisk06l39skhmr3v7lqip0d7mh4f00o.apps.googleusercontent.com` should be:
1. Created in the SAME Google Cloud project as your Android Client ID
2. Type: **Web application** (NOT Android)
3. Visible in **APIs & Services** → **Credentials**

If you don't have a Web Client ID in your project, create one:
1. Click **+ CREATE CREDENTIALS** → **OAuth client ID**
2. Select **Web application**
3. Name it `StudyEngine Web Client`
4. No URI configuration needed (it auto-allows Google Sign-In calls)
5. Click **Create**
6. Copy the Client ID and update your build.gradle.kts

---

## Step 5: Clean Build and Test

```powershell
cd E:\StudyEngine

# Clean all build artifacts
.\gradlew clean

# Rebuild the app
.\gradlew assembleDebug

# Or rebuild and install on device
.\gradlew installDebug
```

Then test signing in with Google.

---

## Troubleshooting Checklist

| Issue | Solution |
|-------|----------|
| Still getting 28444 | 1. Re-verify SHA-1 matches exactly (colons matter!) |
| | 2. Check you're using Web Client ID, not Android Client ID |
| | 3. Verify package name is exactly `com.gatishil.studyengine` |
| | 4. Wait 10-15 minutes for Google Console changes to propagate |
| Using release app but registered debug SHA-1 | Generate release SHA-1, add it to a separate Android Client, OR use debug variant for testing |
| "Invalid client" error | Web Client ID format is wrong or not from same project |
| Google account picker doesn't appear | Check `FilterByAuthorizedAccounts(false)` in AuthViewModel (correct in your code) |

---

## Common Mistakes

❌ **WRONG**: Using Android Client ID in `setServerClientId()`
```kotlin
// ❌ This will fail
.setServerClientId("123456789-abcdefg.apps.googleusercontent.com")  // Android format
```

✅ **CORRECT**: Using Web Client ID
```kotlin
// ✅ This works
.setServerClientId("123456789-abcdefg.apps.googleusercontent.com")  // Web format
```

Both look similar, but they must be from different OAuth client configurations in Google Console.

---

## Quick Reference: What Each Value Must Be

| Value | Your App | Google Console |
|-------|----------|-----------------|
| Package Name | `com.gatishil.studyengine` | Must match exactly |
| SHA-1 (Debug) | From `.\gradlew signingReport` | In Android Client ID |
| Web Client ID | `629081030104-8hisk06l39skhmr3v7lqip0d7mh4f00o.apps.googleusercontent.com` | In Web application Client ID |

---

## Next Steps

1. ✅ Run `.\gradlew signingReport` and get your SHA-1
2. ✅ Go to Google Cloud Console
3. ✅ Verify Android Client ID has correct SHA-1
4. ✅ Verify Web Client ID is in the same project
5. ✅ Run `.\gradlew clean ; .\gradlew assembleDebug`
6. ✅ Test signing in

If error persists after these steps, check:
- Are you using emulator or physical device?
- Is Google Play Services installed on the device?
- Is internet connection working on the device?

