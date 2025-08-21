# DayCall App - ProGuard Rules for Size Optimization
# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ============================================================================
# COMPRESSION OPTIMIZATION
# ============================================================================

# Enable aggressive optimization
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# ============================================================================
# KEEP RULES FOR ESSENTIAL FUNCTIONALITY
# ============================================================================

# Keep Compose-related classes
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-keep @androidx.room.Database class *

# Keep Alarm-related classes
-keep class com.codzuregroup.daycall.alarm.** { *; }
-keep class com.codzuregroup.daycall.data.** { *; }

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }

# Keep WorkManager classes
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# ============================================================================
# REMOVE UNUSED CODE
# ============================================================================

# Remove unused classes
-dontwarn android.support.**
-dontwarn androidx.legacy.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Remove debug information
-keepattributes !SourceFile,!LineNumberTable

# ============================================================================
# STRING OPTIMIZATION
# ============================================================================

# Optimize string constants
-adaptclassstrings
-adaptresourcefilenames
-adaptresourcefilecontents

# ============================================================================
# SPECIFIC EXCLUSIONS
# ============================================================================

# Exclude debug and test classes
-dontwarn junit.**
-dontwarn org.junit.**
-dontwarn org.mockito.**
-dontwarn org.robolectric.**

# Exclude unused Android components
-dontwarn android.app.NotificationManager
-dontwarn android.app.NotificationChannel
-dontwarn android.app.NotificationChannelGroup

# ============================================================================
# KEEP ESSENTIAL ANDROID COMPONENTS
# ============================================================================

# Keep essential Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class * extends android.app.Fragment

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepnames class * implements java.io.Serializable

# ============================================================================
# FINAL OPTIMIZATIONS
# ============================================================================

# Final optimizations
-allowaccessmodification