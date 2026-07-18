package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelId: String = "gemini-3.5-flash",
    val systemInstruction: String? = null
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imagePath: String? = null, // Path to local captured image/file
    val voicePath: String? = null, // Path to voice transcription audio file
    val isAudioResponse: Boolean = false,
    val audioData: String? = null, // Base64 audio for TTS playback
    val thinkingProcess: String? = null, // Thinking process for gemini-3.1-pro thinking mode
    val modelUsed: String? = null
)

@Entity(tableName = "generated_images")
data class GeneratedImageItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prompt: String,
    val size: String = "1K", // "1K", "2K", "4K"
    val imagePath: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Singleton profile
    val name: String = "Sparkex User",
    val email: String = "sparkexaitechnologies@gmail.com",
    val avatarPath: String? = null,
    val subscriptionType: String = "Plus", // "Free", "Plus", "Enterprise"
    val preferredModel: String = "gemini-3.5-flash",
    val thinkingEnabled: Boolean = false,
    val lowLatencyEnabled: Boolean = false,
    val themeMode: String = "System", // "System", "Light", "Dark"
    val selectedLanguage: String = "English",
    val notificationsEnabled: Boolean = true,
    val chatHistoryAutoSave: Boolean = true,
    val voiceType: String = "Amber (Female)",
    val aiMemory: String = ""
)
