#!/bin/bash

# DayCall Version Manager
# 
# This script automates version management following Semantic Versioning (SemVer):
# MAJOR.MINOR.PATCH
# 
# Usage:
# ./version-manager.sh bump major|minor|patch
# ./version-manager.sh set 1.2.3
# ./version-manager.sh info

BUILD_GRADLE_FILE="app/build.gradle.kts"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to get current version
get_current_version() {
    local version_name=$(grep -o 'val appVersionName = "[^"]*"' "$BUILD_GRADLE_FILE" | cut -d'"' -f2)
    local version_code=$(grep -o 'val appVersionCode = [0-9]*' "$BUILD_GRADLE_FILE" | cut -d' ' -f3)
    
    echo "$version_name|$version_code"
}

# Function to parse version components
parse_version() {
    local version=$1
    IFS='.' read -ra VERSION_PARTS <<< "$version"
    echo "${VERSION_PARTS[0]}|${VERSION_PARTS[1]}|${VERSION_PARTS[2]}"
}

# Function to calculate version code
calculate_version_code() {
    local major=$1
    local minor=$2
    local patch=$3
    echo $((major * 10000 + minor * 100 + patch))
}

# Function to update version in build.gradle.kts
update_version() {
    local version_name=$1
    local version_code=$2
    
    # Update version name
    sed -i.bak "s/val appVersionName = \"[^\"]*\"/val appVersionName = \"$version_name\"/" "$BUILD_GRADLE_FILE"
    
    # Update version code
    sed -i.bak "s/val appVersionCode = [0-9]*/val appVersionCode = $version_code/" "$BUILD_GRADLE_FILE"
    
    # Remove backup files
    rm -f "$BUILD_GRADLE_FILE.bak"
    
    echo -e "${GREEN}✅ Version updated to $version_name (build $version_code)${NC}"
}

# Function to update changelog
update_changelog() {
    local version_name=$1
    local date=$(date +"%Y-%m-%d")
    
    local changelog_entry="## [$version_name] - $date

### Added
- New features in this version

### Changed
- Improvements and modifications

### Fixed
- Bug fixes

### Security
- Security updates

---

"
    
    if [ -f "CHANGELOG.md" ]; then
        # Prepend to existing changelog
        echo -e "$changelog_entry$(cat CHANGELOG.md)" > CHANGELOG.md
    else
        # Create new changelog
        cat > CHANGELOG.md << EOF
# Changelog

All notable changes to DayCall will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

$changelog_entry
EOF
    fi
}

# Function to generate release notes
generate_release_notes() {
    local version_name=$1
    local build_number=${BUILD_NUMBER:-1}
    local git_commit=${GIT_COMMIT_HASH:-"dev"}
    local date=$(date +"%Y-%m-%d %H:%M:%S")
    
    cat > "RELEASE_NOTES_${version_name}.md" << EOF
# DayCall ${version_name} Release Notes

## Build Information
- **Version**: ${version_name}
- **Build Number**: ${build_number}
- **Release Date**: ${date}
- **Git Commit**: ${git_commit}

## What's New

### 🎯 New Features
- Enhanced alarm functionality
- Improved challenge system
- Better user experience

### 🔧 Improvements
- Performance optimizations
- Bug fixes and stability improvements
- UI/UX enhancements

### 🐛 Bug Fixes
- Fixed alarm not triggering when app is closed
- Resolved challenge completion issues
- Fixed audio playback problems

## Installation

Download the latest APK from the releases page or install from your preferred app store.

## Support

If you encounter any issues, please report them through the app's feedback system or contact support.
EOF

    echo -e "${GREEN}📝 Release notes generated: RELEASE_NOTES_${version_name}.md${NC}"
}

# Main script logic
case "${1:-}" in
    "info")
        if [ ! -f "$BUILD_GRADLE_FILE" ]; then
            echo -e "${RED}❌ build.gradle.kts not found!${NC}"
            exit 1
        fi
        
        IFS='|' read -r version_name version_code <<< "$(get_current_version)"
        echo -e "${BLUE}📱 DayCall Version Information${NC}"
        echo "Version Name: $version_name"
        echo "Version Code: $version_code"
        echo "Build Number: ${BUILD_NUMBER:-1}"
        echo "Git Commit: ${GIT_COMMIT_HASH:-dev}"
        ;;
        
    "bump")
        if [ -z "$2" ]; then
            echo -e "${RED}❌ Please specify bump type: major, minor, or patch${NC}"
            exit 1
        fi
        
        if [ ! -f "$BUILD_GRADLE_FILE" ]; then
            echo -e "${RED}❌ build.gradle.kts not found!${NC}"
            exit 1
        fi
        
        IFS='|' read -r current_version version_code <<< "$(get_current_version)"
        IFS='|' read -r major minor patch <<< "$(parse_version "$current_version")"
        
        case "$2" in
            "major")
                major=$((major + 1))
                minor=0
                patch=0
                ;;
            "minor")
                minor=$((minor + 1))
                patch=0
                ;;
            "patch")
                patch=$((patch + 1))
                ;;
            *)
                echo -e "${RED}❌ Invalid bump type. Use: major, minor, or patch${NC}"
                exit 1
                ;;
        esac
        
        new_version="$major.$minor.$patch"
        new_version_code=$(calculate_version_code "$major" "$minor" "$patch")
        
        update_version "$new_version" "$new_version_code"
        update_changelog "$new_version"
        ;;
        
    "set")
        if [ -z "$2" ]; then
            echo -e "${RED}❌ Please specify version (e.g., 1.2.3)${NC}"
            exit 1
        fi
        
        if [ ! -f "$BUILD_GRADLE_FILE" ]; then
            echo -e "${RED}❌ build.gradle.kts not found!${NC}"
            exit 1
        fi
        
        # Validate version format
        if [[ ! "$2" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
            echo -e "${RED}❌ Invalid version format. Use: MAJOR.MINOR.PATCH (e.g., 1.2.3)${NC}"
            exit 1
        fi
        
        IFS='|' read -r major minor patch <<< "$(parse_version "$2")"
        version_code=$(calculate_version_code "$major" "$minor" "$patch")
        
        update_version "$2" "$version_code"
        update_changelog "$2"
        ;;
        
    "release")
        if [ ! -f "$BUILD_GRADLE_FILE" ]; then
            echo -e "${RED}❌ build.gradle.kts not found!${NC}"
            exit 1
        fi
        
        IFS='|' read -r version_name version_code <<< "$(get_current_version)"
        generate_release_notes "$version_name"
        ;;
        
    *)
        echo "Usage: ./version-manager.sh <command> [options]"
        echo "Commands:"
        echo "  bump <major|minor|patch>  - Bump version"
        echo "  set <version>             - Set specific version"
        echo "  info                      - Show current version"
        echo "  release                   - Generate release notes"
        ;;
esac 