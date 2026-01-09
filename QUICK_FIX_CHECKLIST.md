# 🚀 ERROR 28444 QUICK FIX CHECKLIST

## Your Values
```
📦 Package Name:        com.gatishil.studyengine
🔑 SHA-1:              2C:1F:64:0D:EF:86:A6:BB:98:B7:5C:C1:5F:AD:09:D9:B9:B9:A0:88
🌐 Web Client ID:      629081030104-8hisk06l39skhmr3v7lqip0d7mh4f00o.apps.googleusercontent.com
```

---

## ✅ QUICK CHECKLIST (5 minutes)

### 1. Google Cloud Console Setup
- [ ] Open [console.cloud.google.com](https://console.cloud.google.com/)
- [ ] Select correct project (ID starts with 629081030104)
- [ ] Go to **APIs & Services** → **Credentials**

### 2. Android Client Verification
- [ ] Find **Android OAuth 2.0 Client ID**
- [ ] **Package Name** = `com.gatishil.studyengine` ✅
- [ ] **SHA-1** = `2C:1F:64:0D:EF:86:A6:BB:98:B7:5C:C1:5F:AD:09:D9:B9:B9:A0:88` ✅
- [ ] If different, click **Edit** and fix it
- [ ] If missing, create new one: **+ CREATE CREDENTIALS** → **Android**

### 3. Web Client Verification  
- [ ] Find **Web OAuth 2.0 Client ID**
- [ ] Client ID = `629081030104-8hisk06l39skhmr3v7lqip0d7mh4f00o.apps.googleusercontent.com` ✅
- [ ] If missing, create new one: **+ CREATE CREDENTIALS** → **Web application**

### 4. API Status
- [ ] Go to **APIs & Services** → **Library**
- [ ] Search "Google Sign-In"
- [ ] Click and verify **ENABLED** ✅

### 5. Rebuild App
```powershell
cd E:\StudyEngine
.\gradlew clean
.\gradlew assembleDebug
```

### 6. Test
- [ ] Run app on device/emulator
- [ ] Tap "Sign In with Google"
- [ ] See account picker (no error 28444) ✅

---

## 🆘 If Still Getting Error 28444

### Option A: Wait & Retry (10-15 minutes)
- Google Console changes take time to sync
- Close the app completely
- Wait 15 minutes
- Rebuild and test again

### Option B: Force Refresh
```powershell
cd E:\StudyEngine
adb uninstall com.gatishil.studyengine  # Remove app
.\gradlew clean                          # Clean build
.\gradlew installDebug                   # Rebuild & reinstall
```

### Option C: Check Logs
1. Open Android Studio
2. Open **Logcat** (bottom panel)
3. Run your app and attempt sign-in
4. Search for "DEVELOPER_ERROR" or "28444"
5. Read the error message - it often hints at what's wrong

---

## 🔍 Troubleshooting Map

```
Is error mentioning wrong SHA-1?
├─ YES → Update SHA-1 in Google Console to: 2C:1F:64:0D:EF:86:A6:BB:98:B7:5C:C1:5F:AD:09:D9:B9:B9:A0:88
└─ NO → Continue

Is error mentioning wrong package name?
├─ YES → Verify Google Console has: com.gatishil.studyengine
└─ NO → Continue

Is error mentioning "API not enabled"?
├─ YES → Go to APIs & Services → Library → Enable Google Sign-In
└─ NO → Continue

Is error mentioning "Invalid client"?
├─ YES → Verify you're using Web Client ID, not Android Client ID
└─ NO → All good! Try again in 15 minutes

Still getting error?
└─ → Check Logcat for exact error, compare values above
```

---

## 📋 Code Configuration Verification

Your `build.gradle.kts` should have:
```kotlin
✅ applicationId = "com.gatishil.studyengine"
✅ buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", 
   "\"629081030104-8hisk06l39skhmr3v7lqip0d7mh4f00o.apps.googleusercontent.com\"")
```

Your `AuthViewModel.kt` should have:
```kotlin
✅ .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
✅ Enhanced error messages for debugging
```

---

## 🎯 Most Common Mistakes

| Mistake | Fix |
|---------|-----|
| Using Android Client ID in code | Use Web Client ID instead |
| SHA-1 in Console ≠ Actual SHA-1 | Update Console with: 2C:1F:64:0D:EF:86:A6:BB:98:B7:5C:C1:5F:AD:09:D9:B9:B9:A0:88 |
| Different Google projects for Android & Web clients | Put both in same project |
| App not uninstalled before rebuild | Run: `adb uninstall com.gatishil.studyengine` |
| Credentials cached in emulator | Use physical device for testing |
| Not waiting for Console to sync | Wait 15 minutes after changes |

---

## ✨ You're All Set If

- ✅ Android Client in Google Console has correct SHA-1
- ✅ Android Client has correct package name
- ✅ Web Client exists in same project
- ✅ Google Sign-In API is enabled
- ✅ App is rebuilt after any Console changes
- ✅ You see account picker when clicking sign-in button

---

## 📞 Still Stuck?

1. Take a screenshot of **Google Console** → **Credentials** page
2. Take a screenshot of **Android Studio** → **Logcat** showing the error
3. Verify all 5 items in "QUICK CHECKLIST" above
4. Compare your values with the "Your Values" section at the top
5. If all match and still failing, check Logcat for the specific error message

**Most likely**: You've missed updating one of the values in Google Console. Double-check all three:
- Package name
- SHA-1 fingerprint
- API enabled status

