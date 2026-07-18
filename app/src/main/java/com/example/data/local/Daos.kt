package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatSessionDao {
    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionByIdSync(sessionId: String): ChatSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSession)

    @Update
    suspend fun updateSession(session: ChatSession)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: String)

    @Query("DELETE FROM chat_sessions")
    suspend fun deleteAllSessions()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesBySession(sessionId: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesBySessionSync(sessionId: String): List<ChatMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessageById(id: Long)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesBySession(sessionId: String)
}

@Dao
interface GeneratedImageDao {
    @Query("SELECT * FROM generated_images ORDER BY timestamp DESC")
    fun getAllImages(): Flow<List<GeneratedImageItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: GeneratedImageItem)

    @Query("DELETE FROM generated_images WHERE id = :id")
    suspend fun deleteImageById(id: Long)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)
}
