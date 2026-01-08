# Google Sign-In Setup Guide for StudyEngine

## Current Configuration Status

✅ **Already Configured:**
1. HTTP support enabled for `http://192.168.0.103:8082/api/`
2. Network security config properly set up
3. `testSignInWithGoogle()` function implemented (no backend or Google form)
4. Web Client ID configured in build.gradle.kts
5. Google Credential Manager integrated

## What You Need to Do

### Step 1: Get Your SHA-1 Fingerprint

Since `keytool` is not in your PATH, use one of these methods:

#### Method A: Using Gradle (Recommended)
Open PowerShell in your project directory and run:
```powershell
cd E:\StudyEngine
.\gradlew signingReport
```

Look for output like:
```
Variant: debug
Config: debug
Store: C:\Users\YourName\.android\debug.keystore
Alias: AndroidDebugKey
SHA1: AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD
```

Copy the SHA-1 value.

#### Method B: Using Full Path to keytool
Find your JDK path first:
```powershell
echo $env:JAVA_HOME
```

Then run (replace path if needed):
```powershell
& "$env:JAVA_HOME\bin\keytool.exe" -keystore "$env:USERPROFILE\.android\debug.keystore" -list -v -alias androiddebugkey -storepass android -keypass android
```

#### Method C: From Android Studio
1. Open **View** → **Tool Windows** → **Gradle**
2. Navigate to: **StudyEngine** → **app** → **Tasks** → **android** → **signingReport**
3. Double-click `signingReport`
4. Check the **Run** tab at the bottom for the SHA-1

### Step 2: Configure Google Cloud Console

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project or create a new one
3. Enable **Google Sign-In API**:
   - Go to **APIs & Services** → **Library**
   - Search for "Google Sign-In"
   - Click **Enable**

4. Create OAuth 2.0 Credentials:
   - Go to **APIs & Services** → **Credentials**
   - Click **+ CREATE CREDENTIALS** → **OAuth client ID**
   
5. Create **Android** Client ID:
   - Application type: **Android**
   - Name: `StudyEngine Android`
   - Package name: `com.gatishil.studyengine`
   - SHA-1 certificate fingerprint: *[paste your SHA-1 from Step 1]*
   - Click **Create**

6. Create **Web** Client ID (if not already created):
   - Click **+ CREATE CREDENTIALS** → **OAuth client ID**
   - Application type: **Web application**
   - Name: `StudyEngine Web Client`
   - Click **Create**
   - **Copy the Client ID** (format: `xxxxx.apps.googleusercontent.com`)

### Step 3: Update Your App Configuration

Replace the Web Client ID in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"YOUR_WEB_CLIENT_ID.apps.googleusercontent.com\"")
```

**Important:** Use the **Web Client ID**, NOT the Android Client ID!

### Step 4: Test Without Backend (Current Setup)

Your app is already configured to test without backend:

```kotlin
// In LoginScreen.kt (line 84)
Button(
    onClick = { viewModel.testSignInWithGoogle() },  // ✅ Using test sign-in
    // ...
)
```

This creates a mock user without:
- ❌ No Google Sign-In form popup
- ❌ No backend API call
- ✅ Just creates a test user directly

### Step 5: Switch to Real Google Sign-In (When Ready)

When you want to use real Google Sign-In, change the button onClick:

```kotlin
Button(
    onClick = { viewModel.signInWithGoogle(context) },  // Real Google Sign-In
    // ...
)
```

## HTTP Support Configuration

✅ Your app already supports HTTP for local development:

**AndroidManifest.xml:**
```xml
android:usesCleartextTraffic="true"
android:networkSecurityConfig="@xml/network_security_config"
```

**network_security_config.xml:**
```xml
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">192.168.0.103</domain>
</domain-config>
```

**API Base URL:**
```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://192.168.0.103:8082/api/v1/\"")
```

## Current Sign-In Flow Options

### Option 1: Test Sign-In (Currently Active)
```kotlin
viewModel.testSignInWithGoogle()
```
- No Google popup
- No backend call
- Creates mock user instantly
- Perfect for UI testing

### Option 2: Real Google Sign-In (Backend Required)
```kotlin
viewModel.signInWithGoogle(context)
```
- Shows Google account picker
- Gets ID token from Google
- Sends token to your backend
- Backend validates and creates session

### Option 3: Real Google Sign-In (No Backend - Future Implementation)

If you want Google authentication without backend, you can modify `AuthViewModel.kt`:

```kotlin
fun signInWithGoogleLocal(context: Context) {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setNonce(generateNonce())
                .build()
                
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
                
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            
            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            
            // Create user from Google credential (no backend)
            val user = User(
                id = googleIdTokenCredential.id,
                name = googleIdTokenCredential.displayName ?: "",
                email = googleIdTokenCredential.id,
                profilePictureUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                timeZone = java.util.TimeZone.getDefault().id,
                createdAt = LocalDateTime.now()
            )
            
            _uiState.update {
                it.copy(isLoading = false, user = user, error = null)
            }
            _events.emit(AuthEvent.SignInSuccess)
            
        } catch (e: Exception) {
            val errorMessage = e.message ?: "Sign in failed"
            _uiState.update { it.copy(isLoading = false, error = errorMessage) }
            _events.emit(AuthEvent.ShowError(errorMessage))
        }
    }
}
```

## Troubleshooting

### Issue: "API not enabled" or "DEVELOPER_ERROR"
- Make sure Google Sign-In API is enabled in Google Cloud Console
- Verify SHA-1 matches your keystore
- Check package name is exactly `com.gatishil.studyengine`

### Issue: HTTP requests failing
- Ensure your backend is running on `http://192.168.0.103:8082`
- Check network security config includes the domain
- Verify `android:usesCleartextTraffic="true"` in manifest

### Issue: "keytool not found"
- Use full path: `"$env:JAVA_HOME\bin\keytool.exe"`
- Or use Gradle: `.\gradlew signingReport`
- Or get SHA-1 from Android Studio Gradle panel

## Summary

Your app is currently set up to:
✅ Support HTTP for local API (`http://192.168.0.103:8082/api/`)
✅ Test sign-in without Google popup or backend
✅ Ready for real Google Sign-In (needs SHA-1 setup)

Next steps:
1. Run `.\gradlew signingReport` to get SHA-1
2. Add SHA-1 to Google Cloud Console
3. Verify Web Client ID in build.gradle.kts
4. Choose which sign-in method to use (test, real, or local)

