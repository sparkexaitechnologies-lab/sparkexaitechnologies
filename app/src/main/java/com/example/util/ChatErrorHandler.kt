package com.example.util

import retrofit2.HttpException
import java.io.IOException

object ChatErrorHandler {
    fun handleException(e: Exception): Exception {
        if (e is HttpException) {
            val code = e.code()
            val message = when (code) {
                400 -> "Bad Request. Please check if your image or prompt is valid."
                401 -> "Authentication failed. Please check your API key in the AI Studio Secrets panel."
                403 -> "Access denied. Please check your Gemini API key permissions."
                429 -> "Rate limit exceeded. Please wait a moment before sending another request to the Gemini API."
                in 500..599 -> "Server error ($code). Google's AI services might be experiencing issues. Please try again later."
                else -> "An unexpected error occurred ($code). Please try again."
            }
            return Exception(message, e)
        }
        if (e is IOException) {
            return Exception("Network error. Please check your internet connection.", e)
        }
        return e
    }
}
