# TwoFactorAuthenticator

A secure, minimal Android OTP authenticator app supporting GitHub and any TOTP-based two-factor authentication. Scan QR codes with your camera to instantly add accounts, or enter secrets manually.

## Features

- **QR Code Scanning** — Use the camera to scan `otpauth://totp/` QR codes from GitHub, Google, Microsoft, and any standard TOTP service
- **Manual Entry** — Add accounts by manually typing the issuer, account name, and Base32 secret key
- **TOTP Generation** — RFC 6238 compliant, generates 6-digit time-based one-time passwords with HMAC-SHA1
- **Real-time Countdown** — Visual progress bar and countdown timer showing remaining validity per code
- **One-tap Copy** — Tap any code card to copy the OTP to clipboard
- **Encrypted Storage** — All secret keys are stored using Android EncryptedSharedPreferences (AES-256)
- **Dark Theme** — Material 3 dark theme optimized for readability
- **Offline** — Works entirely offline; no network permission required

## Screenshots

| Home | Scan | Manual Entry |
|------|------|-------------|
| OTP codes with progress bars | Camera QR code scanner | Manual secret entry form |

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Camera:** CameraX with ZXing for QR decoding
- **Crypto:** javax.crypto (HMAC-SHA1) + AndroidX Security Crypto
- **Architecture:** MVVM with StateFlow
- **Navigation:** Jetpack Navigation Compose

## Building

### Prerequisites

- Android SDK 34+
- JDK 17
- Gradle 8.4+

### Build & Install

```bash
# Clone
git clone https://github.com/arrayforward/TwoFactorAuthenticatorApp.git
cd TwoFactorAuthenticatorApp

# Build debug APK
./gradlew assembleDebug

# Install to connected device
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Release Build

```bash
# Build signed release APK (requires keystore configuration)
./gradlew assembleRelease
```

## Project Structure

```
app/src/main/java/com/otp/authenticator/
├── MainActivity.kt              # Entry point with navigation
├── OtpApp.kt                    # Application class
├── data/
│   ├── OtpAccount.kt            # Data model
│   └── OtpRepository.kt        # Encrypted persistence layer
├── otp/
│   └── TotpGenerator.kt        # RFC 6238 TOTP implementation
├── scanner/
│   └── QrCodeAnalyzer.kt       # CameraX + ZXing QR analyzer
└── ui/
    ├── theme/
    │   ├── Color.kt             # Color definitions
    │   └── Theme.kt             # Material 3 dark theme
    ├── screens/
    │   ├── HomeScreen.kt        # Main OTP list screen
    │   ├── ScanScreen.kt        # Camera QR scanner screen
    │   └── AddAccountScreen.kt  # Manual account entry screen
    └── viewmodel/
        └── MainViewModel.kt     # UI state management
```

## Permissions

- `android.permission.CAMERA` — Required for QR code scanning only

No internet, storage, or other permissions needed.

## License

MIT
