package com.codzuregroup.daycall.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

data class AudioFile(
    val fileName: String,
    val displayName: String,
    val category: AudioCategory,
    val duration: Int = 0,
    val isDefault: Boolean = false
)

enum class AudioCategory {
    WAKE_UP,
    RELAXING,
    ENERGETIC,
    NATURE,
    SCI_FI,
    CINEMATIC,
    IMPACT
}

class AudioManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var previewPlayer: MediaPlayer? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _volume = MutableStateFlow(0.3f)
    val volume: StateFlow<Float> = _volume.asStateFlow()
    
    private val _isPreviewPlaying = MutableStateFlow(false)
    val isPreviewPlaying: StateFlow<Boolean> = _isPreviewPlaying.asStateFlow()
    
    private val _currentAudioFile = MutableStateFlow<AudioFile?>(null)
    val currentAudioFile: StateFlow<AudioFile?> = _currentAudioFile.asStateFlow()

    companion object {
        val availableAudioFiles = listOf(
            // Wake Up Sounds
            AudioFile("labyrinth_for_the_brain_190096.mp3", "Brain Teaser", AudioCategory.WAKE_UP, isDefault = true),
            AudioFile("traimory_mega_horn_angry_siren_f_cinematic_trailer_sound_effects_193408.mp3", "Mega Horn", AudioCategory.WAKE_UP),
            AudioFile("riser_hit_sfx_001_289802.mp3", "Riser Hit", AudioCategory.WAKE_UP),
            AudioFile("riser_wildfire_285209.mp3", "Riser Wildfire", AudioCategory.WAKE_UP),
            
            // Relaxing Sounds
            AudioFile("relaxing_guitar_loop_v5_245859.mp3", "Relaxing Guitar", AudioCategory.RELAXING),
            AudioFile("rainy_day_in_town_with_birds_singing_194011.mp3", "Rainy Day", AudioCategory.RELAXING),
            AudioFile("reliable_safe_327618.mp3", "Reliable Safe", AudioCategory.RELAXING),
            
            // Energetic Sounds
            AudioFile("downfall_3_208028.mp3", "Downfall", AudioCategory.ENERGETIC),
            AudioFile("stab_f_01_brvhrtz_224599.mp3", "Stab Impact", AudioCategory.ENERGETIC),
            AudioFile("large_underwater_explosion_190270.mp3", "Underwater Explosion", AudioCategory.ENERGETIC),
            
            // Nature Sounds
            AudioFile("rainy_day_in_town_with_birds_singing_194011.mp3", "Rainy Day", AudioCategory.NATURE),
            
            // Sci-Fi Sounds
            AudioFile("sci_fi_sound_effect_designed_circuits_hum_24_200825.mp3", "Sci-Fi Circuits", AudioCategory.SCI_FI),
            AudioFile("sci_fi_sound_effect_designed_circuits_hum_10_200831.mp3", "Circuits Hum", AudioCategory.SCI_FI),
            AudioFile("dark_future_logo_196217.mp3", "Dark Future", AudioCategory.SCI_FI),
            
            // Cinematic Sounds
            AudioFile("cinematic_designed_sci_fi_whoosh_transition_nexawave_228295.mp3", "Cinematic Whoosh", AudioCategory.CINEMATIC),
            AudioFile("sound_design_elements_sfx_ps_022_302865.mp3", "Sound Elements", AudioCategory.CINEMATIC),
            
            // Impact Sounds
            AudioFile("elemental_magic_spell_impact_outgoing_228342.mp3", "Elemental Magic", AudioCategory.IMPACT),
            AudioFile("ascent_braam_magma_brassd_cinematic_trailer_sound_effect.mp3", "Ascent Braam", AudioCategory.IMPACT),
            AudioFile("astral_creepy_dark_logo.mp3", "Astral Creepy", AudioCategory.IMPACT)
        )
        
        fun getAudioFilesByCategory(category: AudioCategory): List<AudioFile> {
            return availableAudioFiles.filter { it.category == category }
        }
        
        fun getDefaultAudioFile(): AudioFile {
            return availableAudioFiles.find { it.isDefault } ?: availableAudioFiles.first()
        }
        
        fun getAudioFileByName(fileName: String): AudioFile? {
            return availableAudioFiles.find { it.fileName == fileName }
        }
    }

    fun playAudio(audioFileName: String?, loop: Boolean = true) {
        stopAudio()
        
        val fileName = audioFileName ?: getDefaultAudioFile().fileName
        val audioFile = getAudioFileByName(fileName) ?: getDefaultAudioFile()
        _currentAudioFile.value = audioFile
        
        try {
            mediaPlayer = MediaPlayer().apply {
                // Get the resource ID for the audio file
                val resourceName = fileName.replace(".mp3", "")
                val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
                
                if (resourceId != 0) {
                    setDataSource(context, Uri.parse("android.resource://${context.packageName}/raw/$resourceName"))
                } else {
                    // Fallback to default audio
                    val defaultResourceName = getDefaultAudioFile().fileName.replace(".mp3", "")
                    setDataSource(context, Uri.parse("android.resource://${context.packageName}/raw/$defaultResourceName"))
                }
                
                isLooping = loop
                setVolume(_volume.value, _volume.value)
                prepare()
                start()
                _isPlaying.value = true
                Log.d("AudioManager", "Playing audio: ${audioFile.displayName} (loop: $loop)")
            }
        } catch (e: Exception) {
            Log.e("AudioManager", "Failed to play audio: $fileName", e)
            playFallbackAudio(loop)
        }
    }
    
    fun playAudioByCategory(category: AudioCategory, loop: Boolean = true) {
        val audioFiles = getAudioFilesByCategory(category)
        if (audioFiles.isNotEmpty()) {
            val randomAudio = audioFiles.random()
            playAudio(randomAudio.fileName, loop)
        }
    }

    fun previewAudio(audioFileName: String?, durationSeconds: Int = 3) {
        stopPreview()
        
        val fileName = audioFileName ?: getDefaultAudioFile().fileName
        val audioFile = getAudioFileByName(fileName) ?: getDefaultAudioFile()
        
        try {
            previewPlayer = MediaPlayer().apply {
                val resourceName = fileName.replace(".mp3", "")
                val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
                
                if (resourceId != 0) {
                    setDataSource(context, Uri.parse("android.resource://${context.packageName}/raw/$resourceName"))
                } else {
                    // Fallback to default audio
                    val defaultResourceName = getDefaultAudioFile().fileName.replace(".mp3", "")
                    setDataSource(context, Uri.parse("android.resource://${context.packageName}/raw/$defaultResourceName"))
                }
                
                isLooping = false
                setVolume(0.5f, 0.5f) // Preview at 50% volume
                prepare()
                start()
                _isPreviewPlaying.value = true
                Log.d("AudioManager", "Previewing audio: ${audioFile.displayName}")
                
                // Stop preview after specified duration
                CoroutineScope(Dispatchers.Main).launch {
                    delay(durationSeconds * 1000L)
                    stopPreview()
                }
            }
        } catch (e: Exception) {
            Log.e("AudioManager", "Failed to preview audio: $fileName", e)
            previewFallbackAudio(durationSeconds)
        }
    }

    private fun playFallbackAudio(loop: Boolean) {
        try {
            mediaPlayer = MediaPlayer().apply {
                val fallbackResourceName = getDefaultAudioFile().fileName.replace(".mp3", "")
                setDataSource(context, Uri.parse("android.resource://${context.packageName}/raw/$fallbackResourceName"))
                isLooping = loop
                setVolume(_volume.value, _volume.value)
                prepare()
                start()
                _isPlaying.value = true
                _currentAudioFile.value = getDefaultAudioFile()
                Log.d("AudioManager", "Playing fallback audio")
            }
        } catch (fallbackException: Exception) {
            Log.e("AudioManager", "Failed to play fallback audio", fallbackException)
        }
    }
    
    private fun previewFallbackAudio(durationSeconds: Int) {
        try {
            previewPlayer = MediaPlayer().apply {
                val fallbackResourceName = getDefaultAudioFile().fileName.replace(".mp3", "")
                setDataSource(context, Uri.parse("android.resource://${context.packageName}/raw/$fallbackResourceName"))
                isLooping = false
                setVolume(0.5f, 0.5f)
                prepare()
                start()
                _isPreviewPlaying.value = true
                
                CoroutineScope(Dispatchers.Main).launch {
                    delay(durationSeconds * 1000L)
                    stopPreview()
                }
            }
        } catch (fallbackException: Exception) {
            Log.e("AudioManager", "Failed to preview fallback audio", fallbackException)
        }
    }

    fun stopAudio() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        _isPlaying.value = false
        _currentAudioFile.value = null
        Log.d("AudioManager", "Audio stopped")
    }

    fun stopPreview() {
        previewPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        previewPlayer = null
        _isPreviewPlaying.value = false
        Log.d("AudioManager", "Preview stopped")
    }

    fun setVolume(volume: Float) {
        _volume.value = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(volume, volume)
        Log.d("AudioManager", "Volume set to: $volume")
    }

    fun increaseVolume() {
        val newVolume = (_volume.value + 0.1f).coerceAtMost(1.0f)
        setVolume(newVolume)
        Log.d("AudioManager", "Volume increased to: $newVolume")
    }
    
    fun decreaseVolume() {
        val newVolume = (_volume.value - 0.1f).coerceAtLeast(0.0f)
        setVolume(newVolume)
        Log.d("AudioManager", "Volume decreased to: $newVolume")
    }
    
    fun setVolumeGradually(targetVolume: Float, durationMs: Long = 3000) {
        val startVolume = _volume.value
        val volumeStep = (targetVolume - startVolume) / (durationMs / 100)
        
        CoroutineScope(Dispatchers.Main).launch {
            var currentVolume = startVolume
            while (currentVolume != targetVolume) {
                currentVolume = (currentVolume + volumeStep).coerceIn(0f, targetVolume)
                setVolume(currentVolume)
                delay(100)
            }
        }
    }

    fun isAudioPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    fun isPreviewPlaying(): Boolean {
        return previewPlayer?.isPlaying == true
    }
    
    fun getCurrentAudioFile(): AudioFile? {
        return _currentAudioFile.value
    }
    
    fun getAudioFilesByCategory(category: AudioCategory): List<AudioFile> {
        return availableAudioFiles.filter { it.category == category }
    }
    
    fun getRandomAudioByCategory(category: AudioCategory): AudioFile? {
        val audioFiles = getAudioFilesByCategory(category)
        return if (audioFiles.isNotEmpty()) audioFiles.random() else null
    }
} 