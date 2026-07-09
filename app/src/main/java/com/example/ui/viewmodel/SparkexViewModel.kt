package com.example.ui.viewmodel

import android.app.Application
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatMessage
import com.example.data.local.ChatSession
import com.example.data.local.GeneratedImageItem
import com.example.data.local.UserProfile
import com.example.data.repository.SparkexRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class SparkexViewModel(
    application: Application,
    private val repository: SparkexRepository
) : AndroidViewModel(application) {

    private val tag = "SparkexViewModel"

    // Observed collections
    val sessions: StateFlow<List<ChatSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val generatedImages: StateFlow<List<GeneratedImageItem>> = repository.allGeneratedImages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile> = repository.userProfileFlow
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    // Active session state
    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

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

    fun deleteGeneratedImage(item: GeneratedImageItem) {
        viewModelScope.launch {
            repository.deleteGeneratedImage(item.id, item.imagePath)
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
        viewModelScope.launch {
            val model = userProfile.value.preferredModel
            val session = repository.createNewSession(title, model)
            _activeSessionId.value = session.id

            _isGenerating.value = true
            val result = repository.sendChatMessage(
                sessionId = session.id,
                userPrompt = prompt,
                attachedImagePath = null,
                attachedVoicePath = null,
                modelToUse = model,
                thinkingEnabled = _deepResearchEnabled.value,
                ttsEnabled = _ttsEnabled.value
            )
            _isGenerating.value = false

            if (result.isSuccess && _ttsEnabled.value) {
                val lastMsg = activeMessages.value.lastOrNull { it.role == "model" }
                if (lastMsg != null && lastMsg.isAudioResponse && lastMsg.voicePath != null) {
                    playMessageVoice(lastMsg)
                }
            }
        }
    }

    fun sendTextMessage(
        text: String,
        attachedImagePath: String? = null,
        attachedVoicePath: String? = null
    ) {
        val currentSessionId = _activeSessionId.value
        viewModelScope.launch {
            val sessionId = if (currentSessionId.isNullOrEmpty()) {
                val title = if (text.length > 20) text.take(20) + "..." else text
                val session = repository.createNewSession(title, userProfile.value.preferredModel)
                _activeSessionId.value = session.id
                session.id
            } else {
                currentSessionId
            }

            _isGenerating.value = true
            val result = repository.sendChatMessage(
                sessionId = sessionId,
                userPrompt = text,
                attachedImagePath = attachedImagePath,
                attachedVoicePath = attachedVoicePath,
                modelToUse = userProfile.value.preferredModel,
                thinkingEnabled = _deepResearchEnabled.value,
                ttsEnabled = _ttsEnabled.value
            )
            _isGenerating.value = false

            if (result.isSuccess && _ttsEnabled.value) {
                // Auto-play TTS if audio voice data was fetched
                val lastMsg = activeMessages.value.lastOrNull { it.role == "model" }
                if (lastMsg != null && lastMsg.isAudioResponse && lastMsg.voicePath != null) {
                    playMessageVoice(lastMsg)
                }
            }
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
                else -> "gemini-3.1-flash-lite"
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
    }

    override fun onCleared() {
        super.onCleared()
        stopVoicePlayback()
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
