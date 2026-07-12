import re

with open('app/src/main/java/com/example/ui/viewmodel/SparkexViewModel.kt', 'r') as f:
    content = f.read()

# Add _currentStreamingMessage and generatingJob
state_flow = """    // Active session state
    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

    private val _currentStreamingMessage = MutableStateFlow<ChatMessage?>(null)
    val currentStreamingMessage: StateFlow<ChatMessage?> = _currentStreamingMessage.asStateFlow()
    
    private var generatingJob: kotlinx.coroutines.Job? = null
"""
content = content.replace("    // Active session state\n    private val _activeSessionId = MutableStateFlow<String?>(null)\n    val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()", state_flow)

# Add stopGenerating
stop_func = """    fun stopGenerating() {
        generatingJob?.cancel()
        _isGenerating.value = false
        _currentStreamingMessage.value?.let { msg ->
            viewModelScope.launch {
                repository.addMessage(msg)
                _currentStreamingMessage.value = null
            }
        }
    }
"""

content = content.replace("    fun deleteGeneratedImage", stop_func + "\n    fun deleteGeneratedImage")

# Update startNewSessionWithPrompt
start_new = """    fun startNewSessionWithPrompt(title: String, prompt: String) {
        generatingJob = viewModelScope.launch {
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

            if (result.isSuccess) {
                val fullMessage = result.getOrThrow()
                
                // Simulate typing
                val streamMsg = fullMessage.copy(text = "")
                _currentStreamingMessage.value = streamMsg
                
                var currentText = ""
                val words = fullMessage.text.split(Regex("(?<=\\\\s)"))
                for (word in words) {
                    kotlinx.coroutines.delay(20)
                    currentText += word
                    _currentStreamingMessage.value = streamMsg.copy(text = currentText)
                }
                _currentStreamingMessage.value = streamMsg.copy(text = fullMessage.text)
                
                repository.addMessage(_currentStreamingMessage.value!!)
                _currentStreamingMessage.value = null
                
                if (_ttsEnabled.value && fullMessage.isAudioResponse && fullMessage.voicePath != null) {
                    playMessageVoice(fullMessage)
                }
            }
            _isGenerating.value = false
        }
    }"""
content = re.sub(r'    fun startNewSessionWithPrompt.*?if \(result\.isSuccess && _ttsEnabled\.value\) \{.*?\}[\s\S]*?\}', start_new, content, flags=re.DOTALL)

# Update sendTextMessage
send_text = """    fun sendTextMessage(
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

            val result = repository.sendChatMessage(
                sessionId = sessionId,
                userPrompt = text,
                attachedImagePath = attachedImagePath,
                attachedVoicePath = attachedVoicePath,
                modelToUse = userProfile.value.preferredModel,
                thinkingEnabled = _deepResearchEnabled.value,
                ttsEnabled = _ttsEnabled.value
            )

            if (result.isSuccess) {
                val fullMessage = result.getOrThrow()
                
                // Simulate typing
                val streamMsg = fullMessage.copy(text = "")
                _currentStreamingMessage.value = streamMsg
                
                var currentText = ""
                val words = fullMessage.text.split(Regex("(?<=\\\\s)"))
                for (word in words) {
                    kotlinx.coroutines.delay(20)
                    currentText += word
                    _currentStreamingMessage.value = streamMsg.copy(text = currentText)
                }
                _currentStreamingMessage.value = streamMsg.copy(text = fullMessage.text)
                
                repository.addMessage(_currentStreamingMessage.value!!)
                _currentStreamingMessage.value = null
                
                if (_ttsEnabled.value && fullMessage.isAudioResponse && fullMessage.voicePath != null) {
                    playMessageVoice(fullMessage)
                }
            }
            _isGenerating.value = false
        }
    }"""
content = re.sub(r'    fun sendTextMessage.*?if \(result\.isSuccess && _ttsEnabled\.value\) \{.*?\}[\s\S]*?\}', send_text, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/viewmodel/SparkexViewModel.kt', 'w') as f:
    f.write(content)
