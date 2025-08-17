# Design Document

## Overview

This design addresses the identified issues in the AudioManager class by implementing clean code practices, proper error handling, consistent state management, and resource cleanup. The fixes will maintain backward compatibility while improving reliability and maintainability.

## Architecture

The AudioManager will maintain its current singleton-like pattern but with improved internal structure:

- **State Management**: Centralized state updates with proper synchronization
- **Resource Management**: Explicit lifecycle management for MediaPlayer instances
- **Error Handling**: Layered error handling with fallback mechanisms
- **Volume Control**: Unified volume control methods with proper bounds checking

## Components and Interfaces

### Core Components

1. **MediaPlayer Management**
   - Primary MediaPlayer for main audio playback
   - Preview MediaPlayer for audio previews
   - Proper initialization and cleanup methods

2. **State Management**
   - StateFlow-based reactive state management
   - Consistent state updates across all operations
   - Thread-safe state modifications

3. **Volume Control**
   - Single volume control method with increment/decrement parameters
   - Bounds checking (0.0f to 1.0f)
   - Immediate application to active MediaPlayer instances

4. **Error Handling**
   - Structured exception handling with logging
   - Graceful fallback mechanisms
   - Resource cleanup on errors

### Method Structure

```kotlin
// Volume Control - Consolidated Methods
fun adjustVolume(delta: Float): Boolean
fun increaseVolume(): Boolean  // Calls adjustVolume(0.1f)
fun decreaseVolume(): Boolean  // Calls adjustVolume(-0.1f)

// Resource Management
private fun releaseMediaPlayer()
private fun releasePreviewPlayer()
fun cleanup() // Public cleanup method

// Error Handling
private fun handleAudioError(operation: String, exception: Exception)
private fun playFallbackAudio(loop: Boolean)
```

## Data Models

### State Models
- `isPlaying: StateFlow<Boolean>` - Main audio playback state
- `isPreviewPlaying: StateFlow<Boolean>` - Preview audio state  
- `volume: StateFlow<Float>` - Current volume level (0.0f to 1.0f)

### Audio Configuration
- Audio file list remains as companion object
- Resource URI generation logic centralized
- Fallback audio configuration

## Error Handling

### Error Categories
1. **Resource Loading Errors**: Invalid audio files, missing resources
2. **MediaPlayer Errors**: Playback failures, state conflicts
3. **System Errors**: Audio focus issues, hardware problems

### Error Handling Strategy
```kotlin
try {
    // Primary operation
} catch (e: IOException) {
    handleAudioError("resource_loading", e)
    playFallbackAudio(loop)
} catch (e: IllegalStateException) {
    handleAudioError("mediaplayer_state", e)
    resetAudioState()
} catch (e: Exception) {
    handleAudioError("general", e)
    // Graceful degradation
}
```

### Logging Strategy
- Use Android Log with consistent tags
- Include operation context in error messages
- Log resource cleanup operations
- Debug-level logging for state changes

## Testing Strategy

### Unit Testing Focus
1. **Volume Control Logic**
   - Test volume bounds (0.0f to 1.0f)
   - Test increment/decrement operations
   - Test volume application to MediaPlayer

2. **State Management**
   - Test state transitions during play/stop operations
   - Test concurrent state updates
   - Test state consistency after errors

3. **Resource Management**
   - Test MediaPlayer cleanup
   - Test resource release on errors
   - Test multiple MediaPlayer instance handling

4. **Error Handling**
   - Test fallback mechanisms
   - Test error logging
   - Test graceful degradation

### Integration Testing
- Test with actual audio files
- Test audio focus handling
- Test memory usage during extended playback

### Manual Testing Scenarios
- Play various audio files from the available list
- Test volume controls during playback
- Test preview functionality
- Test error scenarios (missing files, corrupted audio)
- Test rapid play/stop operations
- Test concurrent preview and main audio playback