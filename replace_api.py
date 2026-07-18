import re

with open('app/src/main/java/com/example/data/repository/SparkexRepository.kt', 'r') as f:
    content = f.read()

# Add imports if not present
if "import com.example.data.api.OpenRouter" not in content:
    content = content.replace("import com.example.data.api.*", "import com.example.data.api.*\nimport com.example.data.api.OpenRouterContentPart\nimport com.example.data.api.OpenRouterImageUrl\nimport com.example.data.api.OpenRouterMessage\nimport com.example.data.api.OpenRouterRequest\nimport com.example.data.api.OpenRouterRetrofitClient")

# Replace sendChatMessage
# It's a bit complex, let's just write a full function replacement

new_send_chat = """    suspend fun sendChatMessage(
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

            if (!com.example.util.RequestRateLimiter.isRequestAllowed()) {
                val waitSeconds = com.example.util.RequestRateLimiter.getSecondsToWait()
                return@withContext Result.failure(Exception("Rate limit exceeded. Please wait $waitSeconds seconds before sending another request to protect the API server."))
            }

            var targetModel = modelToUse ?: "google/gemini-2.5-flash"
            if (!targetModel.contains("/")) {
                targetModel = "google/" + targetModel
            }

            val messageHistory = chatMessageDao.getMessagesBySessionSync(sessionId)
            val openRouterMessages = mutableListOf<OpenRouterMessage>()

            val systemInstructionText = "You are Sparkex AI, a helpful and smart AI core engine."
            
            val profileData = userProfileDao.getUserProfile()
            val finalSystemInstruction = if (profileData != null && profileData.aiMemory.isNotBlank()) {
                systemInstructionText + "\\n\\n7. USER MEMORY & PERSONALIZED FACTS:\\n" + profileData.aiMemory
            } else {
                systemInstructionText
            }
            
            openRouterMessages.add(OpenRouterMessage(role = "system", content = listOf(OpenRouterContentPart(type = "text", text = finalSystemInstruction))))

            messageHistory.forEach { msg ->
                val parts = mutableListOf<OpenRouterContentPart>()
                if (msg.role == "user" && msg.imagePath != null) {
                    val base64Img = encodeImageToBase64(msg.imagePath)
                    if (base64Img != null) {
                        parts.add(OpenRouterContentPart(type = "image_url", image_url = OpenRouterImageUrl(url = "data:image/jpeg;base64,$base64Img")))
                    }
                }
                parts.add(OpenRouterContentPart(type = "text", text = msg.text))
                val role = if (msg.role == "model") "assistant" else "user"
                openRouterMessages.add(OpenRouterMessage(role = role, content = parts))
            }

            val request = OpenRouterRequest(
                model = targetModel,
                messages = openRouterMessages,
                stream = false
            )

            var attempts = 0
            val maxAttempts = 3
            var response: com.example.data.api.OpenRouterResponse? = null
            var lastException: Exception? = null
            while (attempts < maxAttempts) {
                try {
                    attempts++
                    response = OpenRouterRetrofitClient.service.generateContent(request)
                    break
                } catch (e: Exception) {
                    lastException = e
                    if (attempts < maxAttempts) {
                        kotlinx.coroutines.delay(400L * attempts)
                    }
                }
            }

            if (response == null) {
                return@withContext Result.failure(lastException ?: Exception("Network request failed after $maxAttempts attempts"))
            }
            if (response.error != null) {
                return@withContext Result.failure(Exception("API Error: ${response.error.message}"))
            }

            val textReply = response.choices?.firstOrNull()?.message?.content ?: "Received empty response."

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
            Log.e(tag, "Error in sendChatMessage", e)
            Result.failure(e)
        }
    }"""

new_send_stream = """    suspend fun sendChatMessageStream(
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
                return@withContext Result.failure(Exception("Gemini API Key is empty."))
            }

            val userMessage = ChatMessage(
                sessionId = sessionId,
                role = "user",
                text = userPrompt,
                imagePath = attachedImagePath,
                voicePath = attachedVoicePath
            )
            chatMessageDao.insertMessage(userMessage)

            if (!com.example.util.RequestRateLimiter.isRequestAllowed()) {
                val waitSeconds = com.example.util.RequestRateLimiter.getSecondsToWait()
                return@withContext Result.failure(Exception("Rate limit exceeded. Please wait $waitSeconds seconds."))
            }

            var targetModel = modelToUse ?: "google/gemini-2.5-flash"
            if (!targetModel.contains("/")) {
                targetModel = "google/" + targetModel
            }

            val messageHistory = chatMessageDao.getMessagesBySessionSync(sessionId)
            val openRouterMessages = mutableListOf<OpenRouterMessage>()

            val systemInstructionText = "You are Sparkex AI, a helpful and smart AI core engine."
            
            val profileData = userProfileDao.getUserProfile()
            val finalSystemInstruction = if (profileData != null && profileData.aiMemory.isNotBlank()) {
                systemInstructionText + "\\n\\n7. USER MEMORY & PERSONALIZED FACTS:\\n" + profileData.aiMemory
            } else {
                systemInstructionText
            }
            
            openRouterMessages.add(OpenRouterMessage(role = "system", content = listOf(OpenRouterContentPart(type = "text", text = finalSystemInstruction))))

            messageHistory.forEach { msg ->
                val parts = mutableListOf<OpenRouterContentPart>()
                if (msg.role == "user" && msg.imagePath != null) {
                    val base64Img = encodeImageToBase64(msg.imagePath)
                    if (base64Img != null) {
                        parts.add(OpenRouterContentPart(type = "image_url", image_url = OpenRouterImageUrl(url = "data:image/jpeg;base64,$base64Img")))
                    }
                }
                parts.add(OpenRouterContentPart(type = "text", text = msg.text))
                val role = if (msg.role == "model") "assistant" else "user"
                openRouterMessages.add(OpenRouterMessage(role = role, content = parts))
            }

            val request = OpenRouterRequest(
                model = targetModel,
                messages = openRouterMessages,
                stream = true
            )

            var attempts = 0
            val maxAttempts = 3
            var response: okhttp3.ResponseBody? = null
            var lastException: Exception? = null
            while (attempts < maxAttempts) {
                try {
                    attempts++
                    response = OpenRouterRetrofitClient.service.generateContentStream(request)
                    break
                } catch (e: Exception) {
                    lastException = e
                    if (attempts < maxAttempts) {
                        kotlinx.coroutines.delay(400L * attempts)
                    }
                }
            }

            if (response == null) {
                throw lastException ?: Exception("Network request failed after $maxAttempts attempts")
            }

            val reader = response.byteStream().bufferedReader()
            var textReply = ""
            
            val responseAdapter = com.squareup.moshi.Moshi.Builder().build().adapter(com.example.data.api.OpenRouterResponse::class.java)

            reader.useLines { lines ->
                for (rawLine in lines) {
                    val trimmed = rawLine.trim()
                    if (trimmed.isEmpty()) continue
                    
                    if (trimmed.startsWith("data:")) {
                        val jsonStr = trimmed.substring(5).trim()
                        if (jsonStr == "[DONE]") continue
                        
                        try {
                            val chunk = responseAdapter.fromJson(jsonStr)
                            val content = chunk?.choices?.firstOrNull()?.delta?.content
                            if (content != null) {
                                textReply += content
                                onChunkReceived(content)
                            }
                        } catch (e: Exception) {
                            // ignore parse error for incomplete chunks
                        }
                    }
                }
            }

            if (textReply.isEmpty()) {
                textReply = "Received empty response."
                onChunkReceived(textReply)
            }

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
    }"""

# Use regex to replace the functions
content = re.sub(r'    suspend fun sendChatMessage\(.*?Result\.success\(aiMessage\)\n        } catch \(e: Exception\) \{\n            Log\.e\(tag, "Error in sendChatMessage", e\)\n            Result\.failure\(e\)\n        \}\n    \}', new_send_chat, content, flags=re.DOTALL)

content = re.sub(r'    suspend fun sendChatMessageStream\(.*?Result\.success\(aiMessage\)\n        } catch \(e: Exception\) \{\n            Log\.e\(tag, "Error in sendChatMessageStream", e\)\n            Result\.failure\(e\)\n        \}\n    \}', new_send_stream, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/data/repository/SparkexRepository.kt', 'w') as f:
    f.write(content)

