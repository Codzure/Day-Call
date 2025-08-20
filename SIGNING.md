# App Signing Guide for DayCall

This document provides instructions for signing the DayCall Android app for release.

## Overview

The app uses Android's app signing system to ensure authenticity and integrity. We support both traditional app signing and Google Play App Signing.

## Keystore Configuration

### 1. Keystore Files

- **Release Keystore**: `daycall-release.keystore` - Main signing key for the app
- **Upload Keystore**: `upload-keystore.jks` - Optional, for Google Play App Signing

### 2. Properties File

The `keystore.properties` file contains sensitive signing information:

```properties
# Main release keystore
storeFile=daycall-release.keystore
storePassword=YOUR_KEYSTORE_PASSWORD
keyAlias=daycall-key
keyPassword=YOUR_KEY_PASSWORD

# Optional: Upload keystore for Play App Signing
# uploadStoreFile=upload-keystore.jks
# uploadStorePassword=UPLOAD_KEYSTORE_PASSWORD
# uploadKeyAlias=upload-key
# uploadKeyPassword=UPLOAD_KEY_PASSWORD
```

**⚠️ IMPORTANT**: Never commit `keystore.properties` or keystore files to version control!

## Building Signed APKs

### Release Builds

```bash
# Build signed release APK for Google Play Store
./gradlew assembleGoogleRelease

# Build signed release APK for Samsung Galaxy Store
./gradlew assembleSamsungRelease

# Build signed release APK for Huawei AppGallery
./gradlew assembleHuaweiRelease
```

### Internal Testing Builds

```bash
# Build signed internal testing APK
./gradlew assembleGoogleInternal
./gradlew assembleSamsungInternal
./gradlew assembleHuaweiInternal
```

### App Bundle (AAB) for Google Play

```bash
# Build signed App Bundle for Google Play Store
./gradlew bundleGoogleRelease

# Build internal testing App Bundle
./gradlew bundleGoogleInternal
```

## Signature Verification

### Verify APK Signature

```bash
# Verify APK signature
jarsigner -verify -verbose -certs app/build/outputs/apk/google/release/DayCall-*.apk

# Check signature details
keytool -printcert -jarfile app/build/outputs/apk/google/release/DayCall-*.apk
```

### Get APK Signature Fingerprints

```bash
# Get SHA-1 fingerprint (for Firebase, Google APIs)
keytool -list -v -keystore daycall-release.keystore -alias daycall-key

# Get SHA-256 fingerprint (for Google Play Console)
keytool -list -v -keystore daycall-release.keystore -alias daycall-key -storepass YOUR_PASSWORD
```

## Security Best Practices

### 1. Keystore Security

- **Backup**: Store keystore files in multiple secure locations
- **Password Management**: Use strong, unique passwords
- **Access Control**: Limit access to keystore files
- **Version Control**: Never commit keystores or passwords

### 2. Environment Variables (Optional)

For CI/CD environments, you can use environment variables:

```bash
export KEYSTORE_FILE=/path/to/keystore
export KEYSTORE_PASSWORD=your_password
export KEY_ALIAS=your_alias
export KEY_PASSWORD=your_key_password
```

### 3. Google Play App Signing

For enhanced security, consider using Google Play App Signing:

1. Generate an upload keystore
2. Sign APKs with upload keystore
3. Let Google Play sign with app signing key

## Build Variants

The app supports multiple build variants:

### Build Types
- **debug**: Debug builds (unsigned)
- **release**: Production releases (signed)
- **internal**: Internal testing (signed)

### Product Flavors
- **google**: Google Play Store
- **samsung**: Samsung Galaxy Store
- **huawei**: Huawei AppGallery

### Example Build Commands

```bash
# Google Play Store release
./gradlew assembleGoogleRelease
./gradlew bundleGoogleRelease

# Samsung Galaxy Store release
./gradlew assembleSamsungRelease

# Huawei AppGallery release
./gradlew assembleHuaweiRelease

# Internal testing for all stores
./gradlew assembleInternalRelease
```

## Troubleshooting

### Common Issues

1. **Keystore not found**
   - Ensure `keystore.properties` exists in project root
   - Check file paths in properties file

2. **Wrong password**
   - Verify keystore and key passwords
   - Use `keytool -list` to test keystore access

3. **Build fails with signing error**
   - Check keystore file permissions
   - Verify alias name matches keystore

### Verification Commands

```bash
# Test keystore access
keytool -list -keystore daycall-release.keystore

# Verify properties file
cat keystore.properties

# Check build configuration
./gradlew signingReport
```

## Distribution

### Google Play Store
- Use signed AAB files
- Upload to Play Console
- Follow Play Store guidelines

### Samsung Galaxy Store
- Use signed APK files
- Submit to Samsung Galaxy Store Developer Portal

### Huawei AppGallery
- Use signed APK files
- Submit to Huawei Developer Console

## Key Information

- **Keystore Type**: JKS
- **Key Algorithm**: RSA
- **Key Size**: 2048 bits
- **Validity**: 25,000 days (~68 years)
- **Signature Versions**: V1, V2, V3, V4 enabled

## Contact

For signing-related issues, contact the development team.

**Remember**: Keep your keystores and passwords secure!

