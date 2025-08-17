# Implementation Plan

- [ ] 1. Remove duplicate methods and clean up method signatures
  - Remove the duplicate `increaseVolume()` method at the end of the class
  - Ensure all method signatures are unique and properly defined
  - _Requirements: 1.1, 1.2, 1.3_

- [ ] 2. Implement consolidated volume control methods
  - Create a private `adjustVolume(delta: Float)` method with bounds checking
  - Refactor existing `increaseVolume()` to use the new adjust method
  - Add `decreaseVolume()` method that calls `adjustVolume(-0.1f)`
  - Ensure volume changes are immediately applied to active MediaPlayer instances
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [ ] 3. Improve error handling and logging
  - Add consistent error logging with meaningful messages throughout the class
  - Create a private `handleAudioError(operation: String, exception: Exception)` method
  - Improve exception handling in `playAudio()` and `previewAudio()` methods
  - Add proper error handling for MediaPlayer state conflicts
  - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [ ] 4. Enhance resource management and cleanup
  - Create private `releaseMediaPlayer()` and `releasePreviewPlayer()` methods
  - Add a public `cleanup()` method for proper resource disposal
  - Ensure all MediaPlayer instances are properly released before creating new ones
  - Add proper null checks before MediaPlayer operations
  - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [ ] 5. Fix state management consistency
  - Ensure state updates happen immediately when audio operations start/stop
  - Add state reset logic in error handling paths
  - Verify state consistency in all MediaPlayer lifecycle events
  - Add proper state management for concurrent operations
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 6. Add coroutine scope management
  - Replace direct CoroutineScope usage with proper scope management
  - Add coroutine cancellation when stopping preview audio
  - Ensure coroutines are properly scoped to prevent memory leaks
  - _Requirements: 5.4_

- [ ] 7. Create unit tests for the fixed AudioManager
  - Write tests for volume control bounds and operations
  - Write tests for state management consistency
  - Write tests for resource cleanup and error handling
  - Write tests for the new consolidated methods
  - _Requirements: All requirements verification_