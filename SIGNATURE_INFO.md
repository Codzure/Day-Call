# DayCall App Signature Information

## 🔑 **Keystore Details**
- **File**: `app/daycall-release.keystore`
- **Alias**: `biasharaai`
- **Algorithm**: RSA 2048-bit
- **Validity**: Valid until January 5, 2053 (28+ years)

## 📱 **Signature Fingerprints**

### SHA-1 (for Firebase, Google APIs, Maps)
```
BB:9B:C3:D1:71:E8:44:8A:94:44:25:49:FF:65:BD:3E:B1:D5:50:49
```

### SHA-256 (for Google Play Console)
```
82:C7:A3:8E:06:D3:9F:A6:3B:AA:5F:BE:B1:02:B3:17:0C:3A:6C:13:CD:33:44:62:93:92:DD:97:49:78:B2:72
```

### MD5 (legacy, rarely used)
```
FE:E3:83:9F:1B:D6:8D:B9:50:64:AA:F9:48:20:29:4E
```

## 🏗️ **Build Commands**

### Quick Build Commands
```bash
# Google Play Store release
./gradlew assembleGoogleRelease bundleGoogleRelease

# Samsung Galaxy Store release
./gradlew assembleSamsungRelease

# Huawei AppGallery release
./gradlew assembleHuaweiRelease

# All store releases
./scripts/build-release.sh release
```

### Using Build Script
```bash
# Single store
./scripts/build-release.sh google
./scripts/build-release.sh samsung
./scripts/build-release.sh huawei

# All releases
./scripts/build-release.sh release

# Internal testing
./scripts/build-release.sh internal
```

## 🔍 **Verification Commands**

### Check Signature Details
```bash
./scripts/get-signatures.sh
```

### Manual Verification
```bash
# Verify APK signature
jarsigner -verify -verbose app/build/outputs/apk/google/release/DayCall-*.apk

# Get keystore info
keytool -list -v -keystore app/daycall-release.keystore
```

## 📊 **File Sizes**
- **Release APK**: ~25MB
- **Debug APK**: ~47MB
- **App Bundle**: ~28MB

## 🔐 **Certificate Information**
- **Subject**: CN=DayCall, OU=CodzureGroup, O=CodzureGroup, L=Nairobi, ST=Nairobi, C=KE
- **Signature Algorithm**: SHA384withRSA
- **Certificate Type**: Self-signed (suitable for Play Store)

## 📋 **Where to Use These Fingerprints**

### SHA-1 Fingerprint
- ✅ Firebase Console
- ✅ Google Sign-In Configuration
- ✅ Google Maps API Console
- ✅ OAuth Client IDs

### SHA-256 Fingerprint
- ✅ Google Play Console (App Signing)
- ✅ Advanced Google Services

## ⚠️ **Security Notes**

1. **Backup Keystore**: Store in multiple secure locations
2. **Password Security**: Use strong passwords, store securely
3. **Never Commit**: Keystore files should never be in version control
4. **Team Access**: Limit access to signing materials
5. **CI/CD**: Use secure environment variables for automation

## 🚀 **Ready for Distribution**

The app is now properly signed and ready for:
- ✅ Google Play Store (APK/AAB)
- ✅ Samsung Galaxy Store (APK)
- ✅ Huawei AppGallery (APK)
- ✅ Internal testing and distribution

---
*Generated on: August 20, 2025*
*DayCall v1.0.1 - Build 1*

