#!/usr/bin/env kotlin

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * DayCall Version Manager
 * 
 * This script automates version management following Semantic Versioning (SemVer):
 * MAJOR.MINOR.PATCH
 * 
 * Usage:
 * ./version-manager.kt bump major|minor|patch
 * ./version-manager.kt set 1.2.3
 * ./version-manager.kt info
 */

@file:DependsOn("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")

data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val buildNumber: Int = 1,
    val gitCommitHash: String = "dev"
) {
    val versionName: String get() = "$major.$minor.$patch"
    val versionCode: Int get() = major * 10000 + minor * 100 + patch
    
    fun bump(type: BumpType): Version {
        return when (type) {
            BumpType.MAJOR -> copy(major = major + 1, minor = 0, patch = 0)
            BumpType.MINOR -> copy(minor = minor + 1, patch = 0)
            BumpType.PATCH -> copy(patch = patch + 1)
        }
    }
    
    override fun toString(): String = "$versionName (build $buildNumber)"
}

enum class BumpType {
    MAJOR, MINOR, PATCH
}

class VersionManager(private val buildGradleFile: File) {
    
    fun getCurrentVersion(): Version {
        val content = buildGradleFile.readText()
        val versionNameMatch = Regex("val appVersionName = \"(\\d+)\\.(\\d+)\\.(\\d+)\"").find(content)
        val versionCodeMatch = Regex("val appVersionCode = (\\d+)").find(content)
        
        if (versionNameMatch == null || versionCodeMatch == null) {
            throw IllegalStateException("Could not parse version from build.gradle.kts")
        }
        
        return Version(
            major = versionNameMatch.groupValues[1].toInt(),
            minor = versionNameMatch.groupValues[2].toInt(),
            patch = versionNameMatch.groupValues[3].toInt(),
            buildNumber = versionCodeMatch.groupValues[1].toInt()
        )
    }
    
    fun updateVersion(version: Version) {
        var content = buildGradleFile.readText()
        
        // Update version name
        content = content.replace(
            Regex("val appVersionName = \"[^\"]*\""),
            "val appVersionName = \"${version.versionName}\""
        )
        
        // Update version code
        content = content.replace(
            Regex("val appVersionCode = \\d+"),
            "val appVersionCode = ${version.versionCode}"
        )
        
        buildGradleFile.writeText(content)
        
        // Update CHANGELOG.md
        updateChangelog(version)
        
        println("✅ Version updated to ${version.versionName} (build ${version.versionCode})")
    }
    
    private fun updateChangelog(version: Version) {
        val changelogFile = File("CHANGELOG.md")
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        
        val newEntry = """
            ## [${version.versionName}] - $date
            
            ### Added
            - New features in this version
            
            ### Changed
            - Improvements and modifications
            
            ### Fixed
            - Bug fixes
            
            ### Security
            - Security updates
            
            ---
            
        """.trimIndent()
        
        if (changelogFile.exists()) {
            val content = changelogFile.readText()
            changelogFile.writeText("$newEntry\n\n$content")
        } else {
            changelogFile.writeText("""
                # Changelog
                
                All notable changes to DayCall will be documented in this file.
                
                The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
                and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
                
                $newEntry
                
            """.trimIndent())
        }
    }
    
    fun generateReleaseNotes(version: Version): String {
        return """
            # DayCall ${version.versionName} Release Notes
            
            ## Build Information
            - **Version**: ${version.versionName}
            - **Build Number**: ${version.buildNumber}
            - **Release Date**: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}
            - **Git Commit**: ${version.gitCommitHash}
            
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
            
        """.trimIndent()
    }
}

fun main(args: Array<String>) {
    val buildGradleFile = File("app/build.gradle.kts")
    
    if (!buildGradleFile.exists()) {
        println("❌ build.gradle.kts not found!")
        System.exit(1)
    }
    
    val versionManager = VersionManager(buildGradleFile)
    
    when {
        args.isEmpty() -> {
            println("Usage: ./version-manager.kt <command> [options]")
            println("Commands:")
            println("  bump <major|minor|patch>  - Bump version")
            println("  set <version>             - Set specific version")
            println("  info                      - Show current version")
            println("  release                   - Generate release notes")
        }
        
        args[0] == "info" -> {
            val version = versionManager.getCurrentVersion()
            println("📱 DayCall Version Information")
            println("Version Name: ${version.versionName}")
            println("Version Code: ${version.versionCode}")
            println("Build Number: ${version.buildNumber}")
            println("Git Commit: ${version.gitCommitHash}")
        }
        
        args[0] == "bump" && args.size > 1 -> {
            val currentVersion = versionManager.getCurrentVersion()
            val bumpType = when (args[1].lowercase()) {
                "major" -> BumpType.MAJOR
                "minor" -> BumpType.MINOR
                "patch" -> BumpType.PATCH
                else -> {
                    println("❌ Invalid bump type. Use: major, minor, or patch")
                    System.exit(1)
                }
            }
            
            val newVersion = currentVersion.bump(bumpType)
            versionManager.updateVersion(newVersion)
        }
        
        args[0] == "set" && args.size > 1 -> {
            val versionString = args[1]
            val versionParts = versionString.split(".")
            
            if (versionParts.size != 3) {
                println("❌ Invalid version format. Use: MAJOR.MINOR.PATCH (e.g., 1.2.3)")
                System.exit(1)
            }
            
            try {
                val version = Version(
                    major = versionParts[0].toInt(),
                    minor = versionParts[1].toInt(),
                    patch = versionParts[2].toInt()
                )
                versionManager.updateVersion(version)
            } catch (e: NumberFormatException) {
                println("❌ Invalid version numbers")
                System.exit(1)
            }
        }
        
        args[0] == "release" -> {
            val version = versionManager.getCurrentVersion()
            val releaseNotes = versionManager.generateReleaseNotes(version)
            
            val releaseFile = File("RELEASE_NOTES_${version.versionName}.md")
            releaseFile.writeText(releaseNotes)
            
            println("📝 Release notes generated: ${releaseFile.name}")
        }
        
        else -> {
            println("❌ Unknown command: ${args[0]}")
            System.exit(1)
        }
    }
} 