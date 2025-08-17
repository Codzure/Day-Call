package com.codzuregroup.daycall.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TextToSpeechManager(private val context: Context) {
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    
    init {
        initializeTTS()
    }
    
    private fun initializeTTS() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TextToSpeechManager", "Language not supported")
                } else {
                    isInitialized = true
                    // Set speech rate to be slightly slower for clarity
                    textToSpeech?.setSpeechRate(0.9f)
                    // Set pitch to be pleasant
                    textToSpeech?.setPitch(1.0f)
                }
            } else {
                Log.e("TextToSpeechManager", "Initialization failed")
            }
        }
    }
    
    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (!isInitialized) {
            Log.w("TextToSpeechManager", "TTS not initialized yet")
            return
        }
        
        textToSpeech?.let { tts ->
            // Set utterance progress listener if callback is provided
            if (onComplete != null) {
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    
                    override fun onDone(utteranceId: String?) {
                        onComplete()
                    }
                    
                    override fun onError(utteranceId: String?) {
                        Log.e("TextToSpeechManager", "Error speaking text")
                    }
                })
            }
            
            // Speak the text
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "celebration_message")
        }
    }
    
    fun speakCelebration(userName: String, onComplete: (() -> Unit)? = null) {
        val message = com.codzuregroup.daycall.utils.TimeBasedMessaging.getCelebrationMessage(userName)
        speak(message, onComplete)
    }
    
    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking ?: false
    }
    
    fun stop() {
        textToSpeech?.stop()
    }
    
    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }
}
