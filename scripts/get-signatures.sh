#!/bin/bash

# DayCall Signature Information Script
# This script displays signature information for the DayCall app

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if keystore.properties exists
if [ ! -f "keystore.properties" ]; then
    print_error "keystore.properties file not found!"
    exit 1
fi

# Get keystore information from properties file
KEYSTORE_FILE=$(grep "storeFile=" keystore.properties | cut -d'=' -f2)
KEY_ALIAS=$(grep "keyAlias=" keystore.properties | cut -d'=' -f2)

# Add app/ prefix if not already present
if [[ ! "$KEYSTORE_FILE" =~ ^app/ ]]; then
    KEYSTORE_FILE="app/$KEYSTORE_FILE"
fi

if [ ! -f "$KEYSTORE_FILE" ]; then
    print_error "Keystore file '$KEYSTORE_FILE' not found!"
    exit 1
fi

print_status "DayCall App Signature Information"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

echo "📄 Keystore: $KEYSTORE_FILE"
echo "🔑 Alias: $KEY_ALIAS"
echo ""

print_status "Getting signature fingerprints..."
echo ""

# Get keystore details
keytool -list -v -keystore "$KEYSTORE_FILE" -alias "$KEY_ALIAS" | grep -E "(SHA1|SHA256|MD5)" | while read line; do
    if [[ $line == *"SHA1"* ]]; then
        echo "🔐 SHA-1: $(echo $line | cut -d':' -f2- | tr -d ' ')"
    elif [[ $line == *"SHA256"* ]]; then
        echo "🔐 SHA-256: $(echo $line | cut -d':' -f2- | tr -d ' ')"
    elif [[ $line == *"MD5"* ]]; then
        echo "🔐 MD5: $(echo $line | cut -d':' -f2- | tr -d ' ')"
    fi
done

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

print_status "Usage Information:"
echo "📱 Google Play Console: Use SHA-256 fingerprint"
echo "🔥 Firebase: Use SHA-1 fingerprint"
echo "🗺️  Google Maps API: Use SHA-1 fingerprint"
echo "📧 Google Sign-In: Use SHA-1 fingerprint"
echo ""

# Check for signed APKs
APK_DIR="app/build/outputs/apk"
if [ -d "$APK_DIR" ]; then
    print_status "Checking signed APKs..."
    
    for apk in $(find "$APK_DIR" -name "*.apk" -type f); do
        echo ""
        echo "📱 $(basename "$apk"):"
        
        if jarsigner -verify "$apk" > /dev/null 2>&1; then
            print_success "✓ Signature verified"
            
            # Get APK signature info
            echo "   Signature details:"
            keytool -printcert -jarfile "$apk" | grep -E "(SHA1|SHA256|Owner)" | sed 's/^/   /'
        else
            print_error "✗ Signature verification failed"
        fi
    done
fi

echo ""
print_success "Signature information retrieved successfully!"

echo ""
echo "💡 Tips:"
echo "   • Save these fingerprints securely"
echo "   • Use SHA-256 for Google Play Console app signing"
echo "   • Use SHA-1 for Firebase and Google API configurations"
echo "   • Keep your keystore file backed up in multiple secure locations"
