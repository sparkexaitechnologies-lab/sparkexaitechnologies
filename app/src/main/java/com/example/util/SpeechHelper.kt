package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class SpeechHelper(private val context: Context) {
    private var speechRecognizer: SpeechRecognizer? = null
    var onResult: ((String) -> Unit)? = null
    var onPartialResult: ((String) -> Unit)? = null
    var onError: ((Int, String) -> Unit)? = null
    var onBeginningOfSpeech: (() -> Unit)? = null
    
    private var isListeningState = false

    fun startListening() {
        isListeningState = true
        Handler(Looper.getMainLooper()).post {
            if (!isListeningState) return@post
            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                    speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {}
                        override fun onBeginningOfSpeech() {
                            onBeginningOfSpeech?.invoke()
                        }
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {}
                        override fun onError(error: Int) {
                            val errorMsg = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                                SpeechRecognizer.ERROR_CLIENT -> "Client-side error"
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission denied"
                                SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy"
                                SpeechRecognizer.ERROR_SERVER -> "Server error"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                                else -> "Unknown error"
                            }
                            
                            onError?.invoke(error, errorMsg)
                            
                            // Auto-retry on recoverable errors if still in listening state
                            if (isListeningState && error != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    if (isListeningState) {
                                        if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                                            try {
                                                speechRecognizer?.cancel()
                                            } catch (e: Exception) {
                                                Log.e("SpeechHelper", "Error canceling recognizer", e)
                                            }
                                        }
                                        startListening()
                                    }
                                }, 600)
                            }
                        }
                        override fun onResults(results: Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                onResult?.invoke(matches[0])
                            } else {
                                if (isListeningState) {
                                    startListening()
                                }
                            }
                        }
                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                onPartialResult?.invoke(matches[0])
                            }
                        }
                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    
                    // Multilingual Auto language detection (English, Telugu, Hindi)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-US", "hi-IN", "te-IN"))
                    
                    // Advanced Voice Activity Detection tuning
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1200L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
                    
                    // Dictation / voice-input optimization for background noise robustness
                    putExtra("android.speech.extra.DICTATION_MODE", true)
                }
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e("SpeechHelper", "Error in startListening", e)
            }
        }
    }

    fun stopListening() {
        isListeningState = false
        Handler(Looper.getMainLooper()).post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e("SpeechHelper", "Error in stopListening", e)
            }
        }
    }

    fun destroy() {
        isListeningState = false
        Handler(Looper.getMainLooper()).post {
            try {
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                Log.e("SpeechHelper", "Error in destroy", e)
            }
            speechRecognizer = null
        }
    }
}
