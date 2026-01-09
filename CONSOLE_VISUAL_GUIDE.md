# 🖼️ Google Cloud Console - Step by Step Visual Guide

## Your Exact SHA-1 to Use
```
2C:1F:64:0D:EF:86:A6:BB:98:B7:5C:C1:5F:AD:09:D9:B9:B9:A0:88
```

---

## STEP 1: Open Google Cloud Console

1. Go to https://console.cloud.google.com/
2. Sign in with your Google account
3. In the top-left, you'll see a **dropdown** showing your project name
4. Make sure you have selected the correct project
5. The project ID should start with `629081030104`

```
┌─────────────────────────────────────────────┐
│ My Project ▼                    [Search box] │
│                                             │
│ Navigate to: APIs & Services > Credentials │
└─────────────────────────────────────────────┘
```

---

## STEP 2: Go to Credentials Page

1. On the left sidebar, click **APIs & Services**
2. Then click **Credentials**

You should see a page with:
- OAuth 2.0 Client IDs section
- Create Credentials button

---

## STEP 3: Check/Create Android Client ID

### 3A. Look for Existing Android Client

Look in the OAuth 2.0 Client IDs list for one that says:
- **Type**: Android
- **Name**: Anything (doesn't matter)

### 3B. IF FOUND: Click to Edit

1. Click the Android Client ID
2. You should see:
   ```
   Package name:    com.gatishil.studyengine
   SHA-1:           [Your current SHA-1]
   ```
3. If SHA-1 is different from `2C:1F:64:0D:EF:86:A6:BB:98:B7:5C:C1:5F:AD:09:D9:B9:B9:A0:88`:
   - Click **Edit**
   - Update the SHA-1 field
   - Click **Save**

### 3C. IF NOT FOUND: Create New One

1. Click **+ CREATE CREDENTIALS**
2. Select **OAuth client ID**
3. Select **Android**
4. Fill in:
   ```
   Name:                    StudyEngine Debug
   Package name:            com.gatishil.studyengine
   SHA-1 certificate:       2C:1F:64:0D:EF:86:A6:BB:98:B7:5C:C1:5F:AD:09:D9:B9:B9:A0:88
   ```
5. Click **Create**

---

## STEP 4: Check/Create Web Client ID

### 4A. Look for Existing Web Client

Look in the OAuth 2.0 Client IDs list for one that says:
- **Type**: Web application
- **Name**: Anything (doesn't matter)
- **Client ID**: Should match `629081030104-8hisk06l39skhmr3v7lqip0d7mh4f00o.apps.googleusercontent.com`

### 4B. IF FOUND: Verify It

1. Click the Web Client ID
2. Verify the Client ID matches your build.gradle.kts:
   ```
   629081030104-8hisk06l39skhmr3v7lqip0d7mh4f00o.apps.googleusercontent.com
   ```
3. If it matches, you're good! Close this.
4. If different, update your build.gradle.kts with the one shown here.

### 4C. IF NOT FOUND: Create New One

1. Click **+ CREATE CREDENTIALS**
2. Select **OAuth client ID**
3. Select **Web application**
4. Fill in:
   ```
   Name:                 StudyEngine Web Client
   Authorized redirect:  (leave empty or add http://localhost:8080/)
   ```
5. Click **Create**
6. Copy the **Client ID** shown
7. Update your build.gradle.kts line 25 with this ID:
   ```kotlin
   buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"YOUR_WEB_CLIENT_ID.apps.googleusercontent.com\"")
   ```

---

## STEP 5: Enable Google Sign-In API

1. On the left sidebar, click **APIs & Services**
2. Click **Library** (not Credentials!)
3. In the search box, type: **Google Sign-In**
4. Click the first result: **Google Sign-In API**
5. You should see one of two things:

   **Case A: If you see ENABLE button**:
   - Click **ENABLE**
   - Wait for it to finish

   **Case B: If you see DISABLE button**:
   - Good! It's already enabled
   - You can close this

---

## STEP 6: Verify Correct Project

Make sure both Android and Web clients are in the **SAME** project:

1. Click on Android Client ID
   - At the top, you'll see the **Project ID**
   - Write it down (should start with `629081030104`)

2. Click on Web Client ID
   - At the top, you'll see the **Project ID**
   - Should be the SAME as Android client

If they're different:
- ❌ You need to recreate one in the correct project
- Android and Web clients MUST be in same project

---

## STEP 7: Screenshot Checklist

Take screenshots of:

### Screenshot 1: Android Client Details
```
┌────────────────────────────────────┐
│ Client ID: [long ID]               │
│ Project: [project name]            │
│ Type: Android                      │
│ Package name: com.gatishil...      │
│ SHA-1: 2C:1F:64:0D:EF:86:A6:...   │
└────────────────────────────────────┘
```

### Screenshot 2: Web Client Details
```
┌────────────────────────────────────┐
│ Client ID: 629081030104-...        │
│ Project: [project name]            │
│ Type: Web application              │
│ Authorized redirect: (filled)      │
└────────────────────────────────────┘
```

### Screenshot 3: Google Sign-In API Status
```
┌────────────────────────────────────┐
│ Google Sign-In API                 │
│ Status: ENABLED (or DISABLE button)│
└────────────────────────────────────┘
```

If all three show correct info, you're done with Google Console! ✅

---

## STEP 8: Rebuild Your App

Open PowerShell in your project:

```powershell
cd E:\StudyEngine
.\gradlew clean
.\gradlew assembleDebug
```

Or to install directly:
```powershell
.\gradlew installDebug
```

---

## STEP 9: Test Sign-In

1. Run your app
2. Tap **"Sign In with Google"** button
3. You should see:
   - ✅ Google account picker dialog (no error!)
   - ✅ List of Google accounts on your device
   - ✅ Ability to select and sign in

If you see error 28444:
1. Verify SHA-1 matches exactly (copy-paste to avoid typos)
2. Verify package name is exactly `com.gatishil.studyengine`
3. Wait 15 minutes for Google Console to sync changes
4. Uninstall app, rebuild, and test again

---

## Common Console Mistakes ❌

### Mistake 1: Wrong Project
```
❌ Android Client in Project A
❌ Web Client in Project B
```
Solution: Recreate one in the same project

### Mistake 2: SHA-1 Typo
```
❌ Have:    2C:1F:64:0D:EF:86:A6:BB:98:B7:5C:C1:5F:AD:09:D9:B9:B9:A0:99 (last digit wrong)
✅ Need:    2C:1F:64:0D:EF:86:A6:BB:98:B7:5C:C1:5F:AD:09:D9:B9:B9:A0:88
```
Solution: Copy-paste your SHA-1 to avoid typos

### Mistake 3: Forgot Web Client
```
❌ Have Android Client only
✅ Need both Android and Web clients
```
Solution: Create Web Client if missing

### Mistake 4: API Not Enabled
```
❌ Google Sign-In API: NOT ENABLED
```
Solution: Go to Library and enable it

### Mistake 5: Using Android Client ID in Code
```
❌ setServerClientId("android-client-id.apps...")
✅ setServerClientId("web-client-id.apps...")
```
Solution: Use Web Client ID in your code

---

## Final Checklist Before Testing

- [ ] Android Client SHA-1 = `2C:1F:64:0D:EF:86:A6:BB:98:B7:5C:C1:5F:AD:09:D9:B9:B9:A0:88`
- [ ] Android Client Package = `com.gatishil.studyengine`
- [ ] Web Client exists with ID `629081030104-8hisk06l39skhmr3v7lqip0d7mh4f00o.apps.googleusercontent.com`
- [ ] Both clients in same project
- [ ] Google Sign-In API is ENABLED
- [ ] App rebuilt with `.\gradlew clean ; .\gradlew assembleDebug`
- [ ] App installed on device/emulator
- [ ] 15+ minutes passed since Console changes (if you just updated)

✅ If all checked: Your sign-in should work!

---

## Quick Test Commands

```powershell
# Clean and rebuild
cd E:\StudyEngine
.\gradlew clean
.\gradlew assembleDebug

# Install on device
.\gradlew installDebug

# View logs (if you have Android SDK tools installed)
adb logcat | findstr "google\|DEVELOPER_ERROR\|28444"
```

Good luck! 🚀

