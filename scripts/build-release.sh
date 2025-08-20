#!/bin/bash

# DayCall Release Build Script
# This script builds signed release versions of the DayCall app

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if keystore.properties exists
if [ ! -f "keystore.properties" ]; then
    print_error "keystore.properties file not found!"
    print_error "Please create keystore.properties with your signing configuration."
    exit 1
fi

# Check if keystore file exists
KEYSTORE_FILE=$(grep "storeFile=" keystore.properties | cut -d'=' -f2)

# Add app/ prefix if not already present
if [[ ! "$KEYSTORE_FILE" =~ ^app/ ]]; then
    KEYSTORE_FILE="app/$KEYSTORE_FILE"
fi

if [ ! -f "$KEYSTORE_FILE" ]; then
    print_error "Keystore file '$KEYSTORE_FILE' not found!"
    print_error "Please ensure your keystore file exists and is correctly configured."
    exit 1
fi

print_status "Starting DayCall release build process..."

# Function to build and verify
build_variant() {
    local variant=$1
    local build_type=$2
    
    print_status "Building $variant$build_type..."
    
    if [ "$build_type" = "Release" ]; then
        # Build APK
        ./gradlew assemble$variant$build_type
        
        # Build AAB for Google Play Store
        if [ "$variant" = "Google" ]; then
            ./gradlew bundle$variant$build_type
        fi
    else
        # Build APK only for non-release builds
        ./gradlew assemble$variant$build_type
    fi
    
    print_success "$variant$build_type build completed!"
}

# Function to verify signatures
verify_signatures() {
    local output_dir="app/build/outputs/apk"
    print_status "Verifying APK signatures..."
    
    for apk in $(find $output_dir -name "*.apk" -type f); do
        if jarsigner -verify "$apk" > /dev/null 2>&1; then
            print_success "✓ $(basename "$apk") - Signature verified"
        else
            print_error "✗ $(basename "$apk") - Signature verification failed"
        fi
    done
}

# Function to show build summary
show_summary() {
    print_status "Build Summary:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    # APK outputs
    local apk_dir="app/build/outputs/apk"
    if [ -d "$apk_dir" ]; then
        echo "📱 APK Files:"
        find "$apk_dir" -name "*.apk" -type f | while read apk; do
            local size=$(du -h "$apk" | cut -f1)
            echo "   • $(basename "$apk") ($size)"
        done
    fi
    
    # AAB outputs
    local aab_dir="app/build/outputs/bundle"
    if [ -d "$aab_dir" ]; then
        echo ""
        echo "📦 App Bundle Files:"
        find "$aab_dir" -name "*.aab" -type f | while read aab; do
            local size=$(du -h "$aab" | cut -f1)
            echo "   • $(basename "$aab") ($size)"
        done
    fi
    
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
}

# Main build process
main() {
    case "$1" in
        "release")
            print_status "Building RELEASE versions for all stores..."
            build_variant "Google" "Release"
            build_variant "Samsung" "Release"
            build_variant "Huawei" "Release"
            ;;
        "internal")
            print_status "Building INTERNAL testing versions..."
            build_variant "Google" "Internal"
            build_variant "Samsung" "Internal"
            build_variant "Huawei" "Internal"
            ;;
        "google")
            print_status "Building Google Play Store release..."
            build_variant "Google" "Release"
            ;;
        "samsung")
            print_status "Building Samsung Galaxy Store release..."
            build_variant "Samsung" "Release"
            ;;
        "huawei")
            print_status "Building Huawei AppGallery release..."
            build_variant "Huawei" "Release"
            ;;
        "all")
            print_status "Building ALL variants (release + internal)..."
            build_variant "Google" "Release"
            build_variant "Samsung" "Release"
            build_variant "Huawei" "Release"
            build_variant "Google" "Internal"
            build_variant "Samsung" "Internal"
            build_variant "Huawei" "Internal"
            ;;
        *)
            echo "Usage: $0 {release|internal|google|samsung|huawei|all}"
            echo ""
            echo "Options:"
            echo "  release  - Build release versions for all stores"
            echo "  internal - Build internal testing versions"
            echo "  google   - Build Google Play Store release only"
            echo "  samsung  - Build Samsung Galaxy Store release only"
            echo "  huawei   - Build Huawei AppGallery release only"
            echo "  all      - Build all variants (release + internal)"
            echo ""
            echo "Examples:"
            echo "  $0 release    # Build all store releases"
            echo "  $0 google     # Build Google Play release only"
            echo "  $0 internal   # Build internal testing builds"
            exit 1
            ;;
    esac
    
    # Verify signatures
    verify_signatures
    
    # Show summary
    show_summary
    
    print_success "Build process completed successfully!"
    print_status "Check app/build/outputs/ for the generated files."
}

# Run main function with all arguments
main "$@"
