# DayCall - Wake with Vibes. Live with Intention.

A modern alarm app that ensures you're fully awake by requiring you to solve challenges before dismissing alarms.

## 🚀 Features

- **Persistent Alarms**: Alarms continue until you solve a challenge
- **Multiple Challenge Types**: Math, QR Scan, Memory Match, Shake, Memory, Pattern, Word, Logic
- **Smart Volume Escalation**: Volume increases until challenge completion
- **Vibration Management**: Customizable vibration patterns and intensity
- **Todo Management**: Full CRUD operations with Room database
- **Modern UI**: Material 3 design with Jetpack Compose
- **Multi-Store Support**: Optimized builds for Google Play, Samsung Galaxy Store, Huawei AppGallery

## 📱 Version Information

### Current Version
- **Version Name**: 1.0.0
- **Version Code**: 10000
- **Build Number**: 1
- **Distribution**: google

### Build Variants

| Build Type | Distribution | Application ID | Features |
|------------|--------------|----------------|----------|
| Debug | Google | com.codzuregroup.daycall.debug | Debug logging, crash reporting disabled |
| Internal | Google | com.codzuregroup.daycall.internal | Full logging, staging API |
| Release | Google | com.codzuregroup.daycall | Production optimized |
| Release | Samsung | com.codzuregroup.daycall | Samsung Galaxy Store optimized |
| Release | Huawei | com.codzuregroup.daycall | Huawei AppGallery optimized |

## 🛠️ Development

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11
- Android SDK 36
- Kotlin 2.0.21

### Build Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/DayCall.git
   cd DayCall
   ```

2. **Build the project**
   ```bash
   # Debug build
   ./gradlew assembleGoogleDebug
   
   # Internal testing build
   ./gradlew assembleGoogleInternal
   
   # Release build
   ./gradlew assembleGoogleRelease
   
   # Samsung Galaxy Store build
   ./gradlew assembleSamsungRelease
   
   # Huawei AppGallery build
   ./gradlew assembleHuaweiRelease
   ```

3. **Run tests**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

### Version Management

We use semantic versioning (MAJOR.MINOR.PATCH) with automated version management.

#### Version Bumping
```bash
# Bump patch version (1.0.0 -> 1.0.1)
./scripts/version-manager.kt bump patch

# Bump minor version (1.0.0 -> 1.1.0)
./scripts/version-manager.kt bump minor

# Bump major version (1.0.0 -> 2.0.0)
./scripts/version-manager.kt bump major

# Set specific version
./scripts/version-manager.kt set 1.2.3

# Show current version info
./scripts/version-manager.kt info

# Generate release notes
./scripts/version-manager.kt release
```

#### Version Code Calculation
- **Formula**: `MAJOR * 10000 + MINOR * 100 + PATCH`
- **Example**: Version 1.2.3 = Version Code 10203

## 🔄 CI/CD Pipeline

### Automated Workflows
- **Version Management**: Automatic version bumping on main branch
- **Multi-Platform Builds**: Parallel builds for all distribution channels
- **Testing**: Automated unit and integration tests
- **Release Management**: Automated release creation with release notes
- **Artifact Management**: APK uploads and version tracking

### Build Triggers
- **Push to main**: Automatic patch version bump and release
- **Manual trigger**: Custom version bump and release type selection
- **Tag push**: Release creation from git tags

## 📊 Build Information

### Environment Variables
- `BUILD_NUMBER`: GitHub run number
- `GIT_COMMIT_HASH`: Current git commit hash
- `DISTRIBUTION_CHANNEL`: Target app store (google/samsung/huawei)

### Build Config Fields
- `VERSION_NAME`: Semantic version (e.g., "1.0.0")
- `VERSION_CODE`: Integer version code
- `BUILD_NUMBER`: Build identifier
- `GIT_COMMIT_HASH`: Git commit reference
- `BUILD_DATE`: Build timestamp
- `DISTRIBUTION_CHANNEL`: Target distribution
- `API_BASE_URL`: Environment-specific API endpoint
- `ENABLE_LOGGING`: Debug logging toggle
- `ENABLE_CRASH_REPORTING`: Crash reporting toggle

## 🏗️ Architecture

### Build Variants
```
app/
├── debug/          # Development builds
├── internal/       # Internal testing builds
├── release/        # Production builds
└── flavors/
    ├── google/     # Google Play Store
    ├── samsung/    # Samsung Galaxy Store
    └── huawei/     # Huawei AppGallery
```

### Bundle Optimization
- **Language splits**: Separate APKs for different languages
- **Density splits**: Optimized for different screen densities
- **ABI splits**: Architecture-specific optimizations

## 📋 Release Process

### 1. Development
- Work on feature branches
- Run tests locally
- Create pull requests

### 2. Testing
- Automated CI/CD pipeline runs
- Internal testing builds
- Beta testing with internal builds

### 3. Release
- Merge to main branch
- Automatic version bump
- Build generation for all platforms
- Release creation with notes

### 4. Distribution
- Google Play Store: `assembleGoogleRelease`
- Samsung Galaxy Store: `assembleSamsungRelease`
- Huawei AppGallery: `assembleHuaweiRelease`

## 🔧 Configuration

### Debug Configuration
- Logging enabled
- Crash reporting disabled
- Development API endpoints
- Debuggable builds

### Release Configuration
- Logging disabled
- Crash reporting enabled
- Production API endpoints
- Optimized builds with ProGuard

### Internal Configuration
- Full logging enabled
- Crash reporting enabled
- Staging API endpoints
- Debuggable for testing

## 📈 Version History

See [CHANGELOG.md](CHANGELOG.md) for detailed version history and release notes.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

### Commit Convention
- `feat:` New features
- `fix:` Bug fixes
- `docs:` Documentation changes
- `style:` Code style changes
- `refactor:` Code refactoring
- `test:` Test additions/changes
- `chore:` Build/tooling changes

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Issues**: Report bugs via GitHub Issues
- **Feature Requests**: Submit via GitHub Discussions
- **Documentation**: See [docs/](docs/) directory
- **Version Info**: Check Settings > App Information in the app

---

**DayCall** - Making mornings intentional, one challenge at a time. 