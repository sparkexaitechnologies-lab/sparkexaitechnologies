package com.example.ui.viewmodel

import android.speech.tts.TextToSpeech
import java.util.Locale
import android.app.Application
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatMessage
import com.example.data.local.GeneratedImageItem
import com.example.data.local.UserProfile
import com.example.data.repository.SparkexRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SparkexViewModel(
    application: Application,
    private val repository: SparkexRepository
) : ViewModel() {

    private val tag = "SparkexViewModel"

    private val prefs = application.getSharedPreferences("sparkex_prefs", android.content.Context.MODE_PRIVATE)
    private val _pinnedSessionIds = MutableStateFlow<Set<String>>(emptySet())
    val pinnedSessionIds: StateFlow<Set<String>> = _pinnedSessionIds.asStateFlow()

    private var textToSpeech: TextToSpeech? = null
    private val _isSpeakingTts = MutableStateFlow<Long?>(null)
    val isSpeakingTts: StateFlow<Long?> = _isSpeakingTts.asStateFlow()

    val sessions = repository.allSessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val generatedImages = repository.allGeneratedImages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile> = repository.userProfileFlow
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    // Active session state
    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

    private val _currentStreamingMessage = MutableStateFlow<ChatMessage?>(null)
    val currentStreamingMessage: StateFlow<ChatMessage?> = _currentStreamingMessage.asStateFlow()
    
    private var generatingJob: Job? = null

    // UI States
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage: StateFlow<Boolean> = _isGeneratingImage.asStateFlow()

    private val _imageGenerationError = MutableStateFlow<String?>(null)
    val imageGenerationError: StateFlow<String?> = _imageGenerationError.asStateFlow()

    private val _deepResearchEnabled = MutableStateFlow(false)
    val deepResearchEnabled: StateFlow<Boolean> = _deepResearchEnabled.asStateFlow()

    private val _ttsEnabled = MutableStateFlow(true)
    val ttsEnabled: StateFlow<Boolean> = _ttsEnabled.asStateFlow()

    private val _currentlyPlayingMessageId = MutableStateFlow<Long?>(null)
    val currentlyPlayingMessageId: StateFlow<Long?> = _currentlyPlayingMessageId.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    // Message stream of active session
    val activeMessages: StateFlow<List<ChatMessage>> = _activeSessionId
        .flatMapLatest { sessionId ->
            if (sessionId != null) {
                repository.getMessagesForSession(sessionId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.getOrCreateProfile()
        }
        val savedPins = prefs.getStringSet("pinned_session_ids", emptySet()) ?: emptySet()
        _pinnedSessionIds.value = savedPins

        textToSpeech = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
            } else {
                Log.e(tag, "Failed to initialize TextToSpeech")
            }
        }
    }

    fun togglePinSession(sessionId: String) {
        val current = _pinnedSessionIds.value.toMutableSet()
        if (current.contains(sessionId)) {
            current.remove(sessionId)
        } else {
            current.add(sessionId)
        }
        _pinnedSessionIds.value = current
        prefs.edit().putStringSet("pinned_session_ids", current).apply()
    }

    fun selectSession(sessionId: String?) {
        _activeSessionId.value = sessionId
    }

    fun toggleDeepResearch() {
        _deepResearchEnabled.value = !_deepResearchEnabled.value
    }

    fun toggleTts() {
        _ttsEnabled.value = !_ttsEnabled.value
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_activeSessionId.value == sessionId) {
                _activeSessionId.value = null
            }
        }
    }

    fun renameSession(sessionId: String, newTitle: String) {
        viewModelScope.launch {
            val sessionList = sessions.value
            val session = sessionList.find { it.id == sessionId }
            if (session != null) {
                repository.updateSession(session.copy(title = newTitle))
            }
        }
    }

    fun stopGenerating() {
        generatingJob?.cancel()
        _isGenerating.value = false
        _currentStreamingMessage.value?.let { msg ->
            viewModelScope.launch {
                repository.addMessage(msg)
                _currentStreamingMessage.value = null
            }
        }
    }

    fun deleteGeneratedImage(item: GeneratedImageItem) {
        viewModelScope.launch {
            repository.deleteGeneratedImage(item.id, item.imagePath)
        }
    }

    fun deleteMessageById(id: Long) {
        viewModelScope.launch {
            repository.deleteMessageById(id)
        }
    }

    fun startNewSession(title: String = "Untitled Chat", isDeepResearch: Boolean = false) {
        viewModelScope.launch {
            val model = if (isDeepResearch) "gemini-3.1-pro-preview" else userProfile.value.preferredModel
            val sysInstruction = if (isDeepResearch) "You are conducting high-depth scientific or technical research. Outline your step-by-step thinking in detail." else null
            val session = repository.createNewSession(title, model, sysInstruction)
            _activeSessionId.value = session.id
            if (isDeepResearch) {
                _deepResearchEnabled.value = true
            }
        }
    }

    fun startNewSessionWithPrompt(title: String, prompt: String) {
        generatingJob = viewModelScope.launch {
            val model = userProfile.value.preferredModel
            val session = repository.createNewSession(title, model)
            _activeSessionId.value = session.id
            _isGenerating.value = true

            var currentText = ""
            val result = repository.sendChatMessageStream(
                sessionId = session.id,
                userPrompt = prompt,
                attachedImagePath = null,
                attachedVoicePath = null,
                modelToUse = model,
                thinkingEnabled = _deepResearchEnabled.value,
                ttsEnabled = _ttsEnabled.value,
                onChunkReceived = { chunk ->
                    currentText += chunk
                    val placeholderMsg = com.example.data.local.ChatMessage(
                        id = -1L,
                        sessionId = session.id,
                        role = "model",
                        text = currentText,
                        modelUsed = model
                    )
                    _currentStreamingMessage.value = placeholderMsg
                }
            )

            _currentStreamingMessage.value = null

            if (result.isSuccess) {
                val fullMessage = result.getOrThrow()
                if (_ttsEnabled.value) {
                    toggleTtsSpeaking(fullMessage)
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "An unknown error occurred"
                val errorChatMessage = com.example.data.local.ChatMessage(
                    sessionId = session.id,
                    role = "model",
                    text = "Error: $errorMsg"
                )
                repository.addMessage(errorChatMessage)
            }
            _isGenerating.value = false
        }
    }

    fun sendTextMessage(
        text: String,
        attachedImagePath: String? = null,
        attachedVoicePath: String? = null
    ) {
        val currentSessionId = _activeSessionId.value
        generatingJob = viewModelScope.launch {
            val sessionId = if (currentSessionId.isNullOrEmpty()) {
                val title = if (text.length > 20) text.take(20) + "..." else text
                val session = repository.createNewSession(title, userProfile.value.preferredModel)
                _activeSessionId.value = session.id
                session.id
            } else {
                currentSessionId
            }
            _isGenerating.value = true

            var currentText = ""
            val model = userProfile.value.preferredModel
            val result = repository.sendChatMessageStream(
                sessionId = sessionId,
                userPrompt = text,
                attachedImagePath = attachedImagePath,
                attachedVoicePath = attachedVoicePath,
                modelToUse = model,
                thinkingEnabled = _deepResearchEnabled.value,
                ttsEnabled = _ttsEnabled.value,
                onChunkReceived = { chunk ->
                    currentText += chunk
                    val placeholderMsg = com.example.data.local.ChatMessage(
                        id = -1L,
                        sessionId = sessionId,
                        role = "model",
                        text = currentText,
                        modelUsed = model
                    )
                    _currentStreamingMessage.value = placeholderMsg
                }
            )

            _currentStreamingMessage.value = null

            if (result.isSuccess) {
                val fullMessage = result.getOrThrow()
                if (_ttsEnabled.value) {
                    toggleTtsSpeaking(fullMessage)
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "An unknown error occurred"
                val errorChatMessage = com.example.data.local.ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    text = "Error: $errorMsg"
                )
                repository.addMessage(errorChatMessage)
            }
            _isGenerating.value = false
        }
    }

    fun generateArtImage(prompt: String, resolution: String, onComplete: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isGeneratingImage.value = true
            _imageGenerationError.value = null
            val result = repository.generateImage(prompt, resolution)
            _isGeneratingImage.value = false
            if (result.isSuccess) {
                val path = result.getOrThrow()
                onComplete(path)
            } else {
                _imageGenerationError.value = result.exceptionOrNull()?.message ?: "Unknown error"
            }
        }
    }

    fun generateProfileAvatar(prompt: String) {
        viewModelScope.launch {
            _isGeneratingImage.value = true
            val result = repository.generateImage(prompt, "1K")
            _isGeneratingImage.value = false
            if (result.isSuccess) {
                val path = result.getOrThrow()
                val updatedProfile = userProfile.value.copy(avatarPath = path)
                repository.updateProfile(updatedProfile)
            }
        }
    }

    fun updateProfileNameEmail(name: String, email: String) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(name = name, email = email)
            repository.updateProfile(updated)
        }
    }

    fun updateSubscription(type: String) {
        viewModelScope.launch {
            val normalizedType = when (type) {
                "Plus", "Pro" -> "Pro"
                "Enterprise", "Pro Plus" -> "Pro Plus"
                else -> "Free"
            }
            val model = when (normalizedType) {
                "Pro Plus" -> "gemini-3.1-pro-preview"
                "Pro" -> "gemini-3.5-flash"
                else -> "gemini-3.1-flash-lite-preview"
            }
            val updated = userProfile.value.copy(subscriptionType = normalizedType, preferredModel = model)
            repository.updateProfile(updated)
        }
    }

    fun selectPreferredModel(model: String) {
        viewModelScope.launch {
            val type = when (model) {
                "gemini-3.1-pro-preview" -> "Pro Plus"
                "gemini-3.5-flash" -> "Pro"
                else -> "Free"
            }
            val updated = userProfile.value.copy(preferredModel = model, subscriptionType = type)
            repository.updateProfile(updated)
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(themeMode = mode)
            repository.updateProfile(updated)
        }
    }

    fun updateSelectedLanguage(lang: String) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(selectedLanguage = lang)
            repository.updateProfile(updated)
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(notificationsEnabled = enabled)
            repository.updateProfile(updated)
        }
    }

    fun updateChatHistoryAutoSave(enabled: Boolean) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(chatHistoryAutoSave = enabled)
            repository.updateProfile(updated)
        }
    }

    fun updateVoiceType(voice: String) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(voiceType = voice)
            repository.updateProfile(updated)
        }
    }

    fun playMessageVoice(message: ChatMessage) {
        val path = message.voicePath ?: return
        try {
            stopVoicePlayback()
            stopTtsSpeaking()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                start()
                _currentlyPlayingMessageId.value = message.id
                setOnCompletionListener {
                    _currentlyPlayingMessageId.value = null
                    stopVoicePlayback()
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to play voice message", e)
            _currentlyPlayingMessageId.value = null
        }
    }

    fun toggleTtsSpeaking(message: ChatMessage) {
        if (_isSpeakingTts.value == message.id) {
            stopTtsSpeaking()
        } else {
            stopVoicePlayback()
            stopTtsSpeaking()
            
            _isSpeakingTts.value = message.id
            
            // Clean text from custom markdown blocks/JSON requests before speaking
            val fullText = message.text
            val imageRequestMatch = Regex("\\[IMAGE_REQUEST: (.*?)\\]").find(fullText)
            val mapRequestMatch = Regex("\\[MAP_REQUEST: (.*?)\\]").find(fullText)
            val cardRequestMatch = Regex("\\[CARD_REQUEST: (.*?)\\]").find(fullText)
            var cleanText = fullText
            imageRequestMatch?.let { cleanText = cleanText.replace(it.value, "") }
            mapRequestMatch?.let { cleanText = cleanText.replace(it.value, "") }
            cardRequestMatch?.let { cleanText = cleanText.replace(it.value, "") }
            cleanText = cleanText.trim()

            if (cleanText.isNotEmpty()) {
                val params = android.os.Bundle().apply {
                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, message.id.toString())
                }
                textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, message.id.toString())
                
                textToSpeech?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == message.id.toString()) {
                            _isSpeakingTts.value = null
                        }
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        if (utteranceId == message.id.toString()) {
                            _isSpeakingTts.value = null
                        }
                    }
                    override fun onError(utteranceId: String?, errorCode: Int) {
                        if (utteranceId == message.id.toString()) {
                            _isSpeakingTts.value = null
                        }
                    }
                })
            } else {
                _isSpeakingTts.value = null
            }
        }
    }

    fun stopTtsSpeaking() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.e(tag, "Error stopping TTS", e)
        } finally {
            _isSpeakingTts.value = null
        }
    }

    fun stopVoicePlayback() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e(tag, "Error releasing media player", e)
        } finally {
            mediaPlayer = null
            _currentlyPlayingMessageId.value = null
        }
        stopTtsSpeaking()
    }

    override fun onCleared() {
        super.onCleared()
        stopVoicePlayback()
        try {
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e(tag, "Error shutting down TTS", e)
        }
    }
}

class SparkexViewModelFactory(
    private val application: Application,
    private val repository: SparkexRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SparkexViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SparkexViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
