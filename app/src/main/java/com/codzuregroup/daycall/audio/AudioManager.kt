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

class AudioManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var previewPlayer: MediaPlayer? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _volume = MutableStateFlow(0.3f)
    val volume: StateFlow<Float> = _volume.asStateFlow()
    
    private val _isPreviewPlaying = MutableStateFlow(false)
    val isPreviewPlaying: StateFlow<Boolean> = _isPreviewPlaying.asStateFlow()

    companion object {
        val availableAudioFiles = listOf(
            "labyrinth_for_the_brain_190096.mp3" to "Brain Teaser",
            "sci_fi_sound_effect_designed_circuits_hum_24_200825.mp3" to "Sci-Fi Circuits",
            "cinematic_designed_sci_fi_whoosh_transition_nexawave_228295.mp3" to "Cinematic Whoosh",
            "traimory_mega_horn_angry_siren_f_cinematic_trailer_sound_effects_193408.mp3" to "Mega Horn",
            "downfall_3_208028.mp3" to "Downfall",
            "rainy_day_in_town_with_birds_singing_194011.mp3" to "Rainy Day",
            "dark_future_logo_196217.mp3" to "Dark Future",
            "reliable_safe_327618.mp3" to "Reliable Safe",
            "sci_fi_sound_effect_designed_circuits_hum_10_200831.mp3" to "Circuits Hum",
            "sound_design_elements_sfx_ps_022_302865.mp3" to "Sound Elements",
            "riser_hit_sfx_001_289802.mp3" to "Riser Hit",
            "relaxing_guitar_loop_v5_245859.mp3" to "Relaxing Guitar",
            "riser_wildfire_285209.mp3" to "Riser Wildfire",
            "elemental_magic_spell_impact_outgoing_228342.mp3" to "Elemental Magic",
            "stab_f_01_brvhrtz_224599.mp3" to "Stab Impact",
            "large_underwater_explosion_190270.mp3" to "Underwater Explosion"
        )
    }

    fun playAudio(audioFileName: String?, loop: Boolean = true) {
        stopAudio()
        
        val fileName = audioFileName ?: "labyrinth_for_the_brain_190096.mp3"
        
        try {
            mediaPlayer = MediaPlayer().apply {
                // Remove .mp3 extension for the resource name
                val resourceName = fileName.replace(".mp3", "")
                val uri = Uri.parse("android.resource://${context.packageName}/raw/$resourceName")
                setDataSource(context, uri)
                isLooping = loop
                setVolume(_volume.value, _volume.value)
                prepare()
                start()
                _isPlaying.value = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to default system sound if audio file fails
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, Uri.parse("android.resource://${context.packageName}/raw/labyrinth_for_the_brain_190096"))
                    isLooping = loop
                    setVolume(_volume.value, _volume.value)
                    prepare()
                    start()
                    _isPlaying.value = true
                }
            } catch (fallbackException: Exception) {
                fallbackException.printStackTrace()
            }
        }
    }

    fun previewAudio(audioFileName: String?, durationSeconds: Int = 3) {
        stopPreview()
        
        val fileName = audioFileName ?: "labyrinth_for_the_brain_190096.mp3"
        
        try {
            previewPlayer = MediaPlayer().apply {
                val resourceName = fileName.replace(".mp3", "")
                val uri = Uri.parse("android.resource://${context.packageName}/raw/$resourceName")
                setDataSource(context, uri)
                isLooping = false
                setVolume(0.5f, 0.5f) // Preview at 50% volume
                prepare()
                start()
                _isPreviewPlaying.value = true
                
                // Stop preview after specified duration
                CoroutineScope(Dispatchers.Main).launch {
                    delay(durationSeconds * 1000L)
                    stopPreview()
                }
            }
        } catch (e: Exception) {
            Log.e("AudioManager", "Failed to preview audio: $fileName", e)
            // Fallback to default sound
            try {
                previewPlayer = MediaPlayer().apply {
                    setDataSource(context, Uri.parse("android.resource://${context.packageName}/raw/labyrinth_for_the_brain_190096"))
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
    }

    fun setVolume(volume: Float) {
        _volume.value = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(volume, volume)
    }

    fun increaseVolume() {
        val newVolume = (_volume.value + 0.1f).coerceAtMost(1.0f)
        setVolume(newVolume)
        Log.d("AudioManager", "Volume increased to: $newVolume")
    }

    fun isAudioPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    fun isPreviewPlaying(): Boolean {
        return previewPlayer?.isPlaying == true
    }
} 