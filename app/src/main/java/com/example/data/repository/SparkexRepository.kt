package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.api.*
import com.example.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

class SparkexRepository(
    private val context: Context,
    private val chatSessionDao: ChatSessionDao,
    private val chatMessageDao: ChatMessageDao,
    private val generatedImageDao: GeneratedImageDao,
    private val userProfileDao: UserProfileDao
) {
    private val tag = "SparkexRepository"

    val allSessions: Flow<List<ChatSession>> = chatSessionDao.getAllSessions()
    val allGeneratedImages: Flow<List<GeneratedImageItem>> = generatedImageDao.getAllImages()
    val userProfileFlow: Flow<UserProfile?> = userProfileDao.getUserProfileFlow()

    suspend fun getOrCreateProfile(): UserProfile = withContext(Dispatchers.IO) {
        var profile = userProfileDao.getUserProfile()
        if (profile == null) {
            profile = UserProfile()
            userProfileDao.insertOrUpdateProfile(profile)
        }
        profile
    }

    suspend fun updateProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        userProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun createNewSession(title: String, modelId: String, systemInstruction: String? = null): ChatSession = withContext(Dispatchers.IO) {
        val session = ChatSession(title = title, modelId = modelId, systemInstruction = systemInstruction)
        chatSessionDao.insertSession(session)
        session
    }

    suspend fun updateSession(session: ChatSession) = withContext(Dispatchers.IO) {
        chatSessionDao.updateSession(session)
    }

    suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
        chatSessionDao.deleteSessionById(sessionId)
        chatMessageDao.deleteMessagesBySession(sessionId)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        chatSessionDao.deleteAllSessions()
    }

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> {
        return chatMessageDao.getMessagesBySession(sessionId)
    }

    suspend fun addMessage(message: ChatMessage) = withContext(Dispatchers.IO) {
        chatMessageDao.insertMessage(message)
    }

    suspend fun deleteMessageById(id: Long) = withContext(Dispatchers.IO) {
        chatMessageDao.deleteMessageById(id)
    }

    suspend fun addGeneratedImage(image: GeneratedImageItem) = withContext(Dispatchers.IO) {
        generatedImageDao.insertImage(image)
    }

    suspend fun deleteGeneratedImage(id: Long, path: String) = withContext(Dispatchers.IO) {
        generatedImageDao.deleteImageById(id)
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to delete file: $path", e)
        }
    }

    /**
     * Executes a chat query to Gemini, updating local database.
     * Handles conversation history, image analysis, speech synthesis, and deep reasoning thinking mode.
     */
    suspend fun sendChatMessage(
        sessionId: String,
        userPrompt: String,
        attachedImagePath: String? = null,
        attachedVoicePath: String? = null,
        modelToUse: String? = null,
        thinkingEnabled: Boolean = false,
        ttsEnabled: Boolean = false
    ): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(Exception("Gemini API Key is empty. Please configure it in the AI Studio Secrets panel."))
            }

            // Save user message locally
            val userMessage = ChatMessage(
                sessionId = sessionId,
                role = "user",
                text = userPrompt,
                imagePath = attachedImagePath,
                voicePath = attachedVoicePath
            )
            chatMessageDao.insertMessage(userMessage)

            // Apply client-side rate limit check
            if (!com.example.util.RequestRateLimiter.isRequestAllowed()) {
                val waitSeconds = com.example.util.RequestRateLimiter.getSecondsToWait()
                return@withContext Result.failure(Exception("Rate limit exceeded. Please wait $waitSeconds seconds before sending another request to protect the API server."))
            }

            // Resolve proper model name based on preferences and options
            var targetModel = modelToUse ?: "gemini-3.5-flash"
            if (targetModel == "gemini-3.1-flash-lite") {
                targetModel = "gemini-3.1-flash-lite-preview"
            }
            if (thinkingEnabled) {
                targetModel = "gemini-3.1-pro-preview" // Thinking requires gemini-3.1-pro-preview
            }

            // Get historical context for multi-turn chat
            val messageHistory = chatMessageDao.getMessagesBySessionSync(sessionId)
            val contents = mutableListOf<Content>()

            // Map history to Content structures
            messageHistory.forEach { msg ->
                val parts = mutableListOf<Part>()
                if (msg.role == "user" && msg.imagePath != null) {
                    val base64Img = encodeImageToBase64(msg.imagePath)
                    if (base64Img != null) {
                        parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Img)))
                    }
                }
                parts.add(Part(text = msg.text))
                contents.add(Content(role = msg.role, parts = parts))
            }

            // Configuration options
            var genConfig: GenerationConfig? = null
            if (thinkingEnabled) {
                genConfig = GenerationConfig(
                    thinkingConfig = ThinkingConfig(thinkingLevel = "high")
                    // Do NOT set maxOutputTokens for HIGH thinking as mandated
                )
            } else if (ttsEnabled) {
                genConfig = GenerationConfig(
                    responseModalities = listOf("TEXT", "AUDIO"),
                    speechConfig = SpeechConfig(
                        voiceConfig = VoiceConfig(
                            prebuiltVoiceConfig = PrebuiltVoiceConfig(voiceName = "Kore")
                        )
                    )
                )
            }

            val session = chatSessionDao.getAllSessions() // We could fetch session-specific system instruction if needed
            val systemInstructionText = """You are Sparkex AI, a helpful and smart AI core engine.

1. IDENTITY: If the user asks about your name, model, or engine, you must strictly reply as Sparkex AI. Do not mention Gemini or Google unless asked for technical specifications.
2. ACTION HANDLERS: You will support custom UI triggers. If the user commands an action related to chat management, append the hidden token at the end of your conversational confirmation:
   - For deleting/clearing chat: [ACTION: DELETE_CHAT]
   - For renaming chat: [ACTION: RENAME_CHAT]
   - For sharing chat: [ACTION: SHARE_CHAT]
   - For pinning chat: [ACTION: PIN_CHAT]
3. IMAGE GENERATION: If the user asks to create/generate an image, respond strictly with this format: [IMAGE_REQUEST: detailed description of the scene] followed by a friendly response.
4. MAPS/LOCATION: If the user asks for directions, places, or spots, provide the visual coordinates or location names in this format: [MAP_REQUEST: Location Name, City].
5. TASK SCHEDULING/CARDS: If the user wants to set a daily digest, job tracker, or reminder, return a custom layout token: [CARD_REQUEST: Task Name | Time | Instructions].
6. TONALITY: Keep responses elegant, incredibly fast, and smart, matching a premium flagship AI assistant."""

            val systemInstructionContent = Content(parts = listOf(Part(text = systemInstructionText)))

            val request = GenerateContentRequest(
                contents = contents,
                generationConfig = genConfig,
                systemInstruction = systemInstructionContent
            )

            val response = RetrofitClient.service.generateContent(targetModel, apiKey, request)
            
            if (response.error != null) {
                return@withContext Result.failure(Exception("Gemini API Error: ${response.error.message}"))
            }

            val candidate = response.candidates?.firstOrNull()
            val replyContent = candidate?.content
            var textReply = replyContent?.parts?.firstOrNull { it.text != null }?.text ?: "Received empty response from Sparkex AI."
            
            // Remove markdown formatting symbols completely as requested
            // Removed formatting stripping to allow markdown
            
            // Extract speech or extra audio data if present
            var savedVoicePath: String? = null
            val audioPart = replyContent?.parts?.firstOrNull { it.inlineData?.mimeType?.startsWith("audio") == true }
            if (audioPart != null) {
                val base64Audio = audioPart.inlineData!!.data
                savedVoicePath = saveBase64ToFile(context, base64Audio, "sparkex_tts", "wav")
            }

            // Save AI message to database
            val aiMessage = ChatMessage(
                sessionId = sessionId,
                role = "model",
                text = textReply,
                voicePath = savedVoicePath,
                isAudioResponse = savedVoicePath != null,
                modelUsed = targetModel
            )
            chatMessageDao.insertMessage(aiMessage)

            Result.success(aiMessage)
        } catch (e: Exception) {
            Log.e(tag, "Error in sendChatMessage", e)
            Result.failure(e)
        }
    }

    /**
     * Executes a streaming chat query to Gemini, updating local database.
     * Invokes onChunkReceived callback as new text pieces arrive.
     */
    suspend fun sendChatMessageStream(
        sessionId: String,
        userPrompt: String,
        attachedImagePath: String? = null,
        attachedVoicePath: String? = null,
        modelToUse: String? = null,
        thinkingEnabled: Boolean = false,
        ttsEnabled: Boolean = false,
        onChunkReceived: (String) -> Unit
    ): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(Exception("Gemini API Key is empty. Please configure it in the AI Studio Secrets panel."))
            }

            // Save user message locally
            val userMessage = ChatMessage(
                sessionId = sessionId,
                role = "user",
                text = userPrompt,
                imagePath = attachedImagePath,
                voicePath = attachedVoicePath
            )
            chatMessageDao.insertMessage(userMessage)

            // Apply client-side rate limit check
            if (!com.example.util.RequestRateLimiter.isRequestAllowed()) {
                val waitSeconds = com.example.util.RequestRateLimiter.getSecondsToWait()
                return@withContext Result.failure(Exception("Rate limit exceeded. Please wait $waitSeconds seconds before sending another request to protect the API server."))
            }

            // Resolve proper model name based on preferences and options
            var targetModel = modelToUse ?: "gemini-3.5-flash"
            if (targetModel == "gemini-3.1-flash-lite") {
                targetModel = "gemini-3.1-flash-lite-preview"
            }
            if (thinkingEnabled) {
                targetModel = "gemini-3.1-pro-preview" // Thinking requires gemini-3.1-pro-preview
            }

            // Get historical context for multi-turn chat
            val messageHistory = chatMessageDao.getMessagesBySessionSync(sessionId)
            val contents = mutableListOf<Content>()

            // Map history to Content structures
            messageHistory.forEach { msg ->
                val parts = mutableListOf<Part>()
                if (msg.role == "user" && msg.imagePath != null) {
                    val base64Img = encodeImageToBase64(msg.imagePath)
                    if (base64Img != null) {
                        parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Img)))
                    }
                }
                parts.add(Part(text = msg.text))
                contents.add(Content(role = msg.role, parts = parts))
            }

            // Configuration options
            var genConfig: GenerationConfig? = null
            if (thinkingEnabled) {
                genConfig = GenerationConfig(
                    thinkingConfig = ThinkingConfig(thinkingLevel = "high")
                )
            }

            val systemInstructionText = """You are Sparkex AI, a helpful and smart AI core engine.

1. IDENTITY: If the user asks about your name, model, or engine, you must strictly reply as Sparkex AI. Do not mention Gemini or Google unless asked for technical specifications.
2. ACTION HANDLERS: You will support custom UI triggers. If the user commands an action related to chat management, append the hidden token at the end of your conversational confirmation:
   - For deleting/clearing chat: [ACTION: DELETE_CHAT]
   - For renaming chat: [ACTION: RENAME_CHAT]
   - For sharing chat: [ACTION: SHARE_CHAT]
   - For pinning chat: [ACTION: PIN_CHAT]
3. IMAGE GENERATION: If the user asks to create/generate an image, respond strictly with this format: [IMAGE_REQUEST: detailed description of the scene] followed by a friendly response.
4. MAPS/LOCATION: If the user asks for directions, places, or spots, provide the visual coordinates or location names in this format: [MAP_REQUEST: Location Name, City].
5. TASK SCHEDULING/CARDS: If the user wants to set a daily digest, job tracker, or reminder, return a custom layout token: [CARD_REQUEST: Task Name | Time | Instructions].
6. TONALITY: Keep responses elegant, incredibly fast, and smart, matching a premium flagship AI assistant."""

            val systemInstructionContent = Content(parts = listOf(Part(text = systemInstructionText)))

            val request = GenerateContentRequest(
                contents = contents,
                generationConfig = genConfig,
                systemInstruction = systemInstructionContent
            )

            val response = RetrofitClient.service.generateContentStream(targetModel, apiKey, request)
            val reader = response.byteStream().bufferedReader()
            val jsonBuffer = java.lang.StringBuilder()
            var textReply = ""

            val responseAdapter = com.squareup.moshi.Moshi.Builder().build().adapter(GenerateContentResponse::class.java)

            reader.useLines { lines ->
                for (rawLine in lines) {
                    val trimmed = rawLine.trim()
                    if (trimmed.isEmpty() || trimmed == "[" || trimmed == "]") {
                        continue
                    }
                    val cleanLine = if (trimmed.startsWith(",")) trimmed.substring(1).trim() else trimmed
                    if (cleanLine.isEmpty()) continue

                    jsonBuffer.append(cleanLine)
                    try {
                        var chunkStr = jsonBuffer.toString().trim()
                        if (chunkStr.endsWith(",")) {
                            chunkStr = chunkStr.substring(0, chunkStr.length - 1).trim()
                        }
                        val chunk = responseAdapter.fromJson(chunkStr)
                        if (chunk != null) {
                            val text = chunk.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.text != null }?.text
                            if (text != null) {
                                textReply += text
                                onChunkReceived(text)
                            }
                            jsonBuffer.setLength(0) // Clear buffer on success
                        }
                    } catch (e: Exception) {
                        // Keep accumulating in buffer, as it might be a multi-line JSON
                    }
                }
            }

            if (textReply.isEmpty()) {
                textReply = "Received empty response from Sparkex AI."
                onChunkReceived(textReply)
            }

            // Save AI message to database
            val aiMessage = ChatMessage(
                sessionId = sessionId,
                role = "model",
                text = textReply,
                voicePath = null,
                isAudioResponse = false,
                modelUsed = targetModel
            )
            chatMessageDao.insertMessage(aiMessage)

            Result.success(aiMessage)
        } catch (e: Exception) {
            Log.e(tag, "Error in sendChatMessageStream", e)
            Result.failure(e)
        }
    }

    /**
     * Generates a high-quality image using gemini-3-pro-image-preview
     * Saves the resulting image locally and logs it in the GeneratedImage table.
     */
    suspend fun generateImage(prompt: String, resolution: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Apply client-side rate limit check
            if (!com.example.util.RequestRateLimiter.isRequestAllowed()) {
                val waitSeconds = com.example.util.RequestRateLimiter.getSecondsToWait()
                return@withContext Result.failure(Exception("Rate limit exceeded. Please wait $waitSeconds seconds before generating another image to protect the API server."))
            }

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(Exception("API Key not set. Please use AI Studio Secrets panel."))
            }

            val model = "gemini-3-pro-image-preview"
            val imageSize = when (resolution) {
                "4K" -> "4K"
                "2K" -> "2K"
                else -> "1K"
            }

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(
                    imageConfig = ImageConfig(aspectRatio = "1:1", imageSize = imageSize),
                    responseModalities = listOf("TEXT", "IMAGE")
                )
            )

            val response = RetrofitClient.service.generateContent(model, apiKey, request)
            if (response.error != null) {
                return@withContext Result.failure(Exception("Gemini API Error: ${response.error.message}"))
            }

            val candidate = response.candidates?.firstOrNull()
            val imgPart = candidate?.content?.parts?.firstOrNull { it.inlineData?.mimeType?.startsWith("image") == true }
                ?: return@withContext Result.failure(Exception("No image data returned from model."))

            val base64Data = imgPart.inlineData!!.data
            val localPath = saveBase64ToFile(context, base64Data, "sparkex_gen", "jpg")

            // Log in Database
            val item = GeneratedImageItem(
                prompt = prompt,
                size = imageSize,
                imagePath = localPath
            )
            generatedImageDao.insertImage(item)

            Result.success(localPath)
        } catch (e: Exception) {
            Log.e(tag, "Image generation error", e)
            Result.failure(e)
        }
    }

    private fun encodeImageToBase64(path: String): String? {
        return try {
            val file = File(path)
            if (!file.exists()) return null
            val bitmap = BitmapFactory.decodeFile(path)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(tag, "Failed to encode image to base64", e)
            null
        }
    }

    private fun saveBase64ToFile(context: Context, base64Str: String, prefix: String, extension: String): String {
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        val dir = File(context.filesDir, "sparkex_assets").apply { mkdirs() }
        val file = File(dir, "${prefix}_${UUID.randomUUID()}.$extension")
        file.writeBytes(bytes)
        return file.absolutePath
    }
}
