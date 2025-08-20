import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
}

// Version configuration
val appVersionCode = 10002
val appVersionName = "1.0.2"
val buildNumber = System.getenv("BUILD_NUMBER")?.toIntOrNull() ?: 1
val gitCommitHash = System.getenv("GIT_COMMIT_HASH") ?: "dev"

android {
    namespace = "com.codzuregroup.daycall"
    compileSdk = 36
    
    // Load keystore properties
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(keystorePropertiesFile.inputStream())
    }

    defaultConfig {
        applicationId = "com.codzuregroup.daycall"
        minSdk = 24
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        // Build info for debugging
        buildConfigField("String", "BUILD_NUMBER", "\"$buildNumber\"")
        buildConfigField("String", "GIT_COMMIT_HASH", "\"$gitCommitHash\"")
        buildConfigField("String", "BUILD_DATE", "\"${System.currentTimeMillis()}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                
                // Enable V1 and V2 signature schemes
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = true
            }
        }
        
        create("upload") {
            if (keystorePropertiesFile.exists() && keystoreProperties.containsKey("uploadStoreFile")) {
                keyAlias = keystoreProperties.getProperty("uploadKeyAlias")
                keyPassword = keystoreProperties.getProperty("uploadKeyPassword")
                storeFile = file(keystoreProperties.getProperty("uploadStoreFile"))
                storePassword = keystoreProperties.getProperty("uploadStorePassword")
                
                // Enable V1 and V2 signature schemes
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = true
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            
            // Debug-specific configurations
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            buildConfigField("boolean", "ENABLE_CRASH_REPORTING", "false")
            buildConfigField("String", "API_BASE_URL", "\"https://api-dev.daycall.com\"")
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Apply signing configuration
            signingConfig = signingConfigs.getByName("release")
            
            // Release-specific configurations
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
            buildConfigField("boolean", "ENABLE_CRASH_REPORTING", "true")
            buildConfigField("String", "API_BASE_URL", "\"https://api.daycall.com\"")
        }
        
        // Internal testing build
        create("internal") {
            initWith(getByName("release"))
            applicationIdSuffix = ".internal"
            versionNameSuffix = "-internal"
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            
            // Apply signing configuration
            signingConfig = signingConfigs.getByName("release")
            
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            buildConfigField("boolean", "ENABLE_CRASH_REPORTING", "true")
            buildConfigField("String", "API_BASE_URL", "\"https://api-staging.daycall.com\"")
        }
    }
    
    // Product flavors for different distribution channels
    flavorDimensions += "distribution"
    
    productFlavors {
        create("google") {
            dimension = "distribution"
            // Google Play Store specific configurations
            buildConfigField("String", "DISTRIBUTION_CHANNEL", "\"google\"")
        }
        
        create("samsung") {
            dimension = "distribution"
            // Samsung Galaxy Store specific configurations
            buildConfigField("String", "DISTRIBUTION_CHANNEL", "\"samsung\"")
        }
        
        create("huawei") {
            dimension = "distribution"
            // Huawei AppGallery specific configurations
            buildConfigField("String", "DISTRIBUTION_CHANNEL", "\"huawei\"")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        animationsDisabled = true
    }

    // Bundle configuration for App Bundle optimization
    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
    
    // APK naming convention
    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val outputFileName = "DayCall-${variant.versionName}-${variant.name}-${buildNumber}.apk"
                output.outputFileName = outputFileName
            }
    }
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains:annotations:23.0.0")
        exclude(group = "com.intellij", module = "annotations")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.foundation:foundation:1.6.7")
    // Material Icons (needed for Icons.Default.*)
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.1")
    implementation("androidx.lifecycle:lifecycle-service:2.9.1")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    
    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Hilt for dependency injection (optional but recommended)
    implementation("com.google.dagger:hilt-android:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation(libs.androidx.junit.ktx)
    implementation(libs.ui.test.junit4)

    // Room compiler
    kapt(libs.androidx.room.compiler)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.com.google.exoplayer)
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.7")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.7")
}
