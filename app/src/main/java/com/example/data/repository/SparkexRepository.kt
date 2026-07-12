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

            // Resolve proper model name based on preferences and options
            var targetModel = modelToUse ?: "gemini-3.5-flash"
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
            val systemInstructionText = """You are Sparkex AI, an elite personal executive assistant. Your task is to generate a highly professional, clean, and structured "Daily Rundown" or morning briefing for the user based on their provided data (reminders, tasks).

Follow this strict layout format:
1. Greeting: Start with "Hi [User's Name], here's your daily rundown 🤸"
2. Top of Mind Section: Highlight the most urgent task or financial action due today. Use a clean bullet point, bold the key numbers/actions, and add relevant action links or sub-notes if available.

Tone Guidelines: Use absolute distinction, refined vocabulary, and keep it distraction-free. Avoid markdown clutter like unnecessary triple asterisks; stick to clean Material 3 design-friendly structuring."""

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
            val textReply = replyContent?.parts?.firstOrNull { it.text != null }?.text ?: "Received empty response from Sparkex AI."
            
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
            // chatMessageDao.insertMessage(aiMessage)

            Result.success(aiMessage)
        } catch (e: Exception) {
            Log.e(tag, "Error in sendChatMessage", e)
            Result.failure(e)
        }
    }

    /**
     * Generates a high-quality image using gemini-3-pro-image-preview
     * Saves the resulting image locally and logs it in the GeneratedImage table.
     */
    suspend fun generateImage(prompt: String, resolution: String): Result<String> = withContext(Dispatchers.IO) {
        try {
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
