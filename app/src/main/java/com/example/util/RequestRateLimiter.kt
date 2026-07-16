package com.example.util

import java.util.concurrent.ConcurrentLinkedQueue

/**
 * A client-side request rate limiter to protect the Sparkex backend (Gemini API)
 * from excessive API calls, ensuring compliance with Gemini API usage policies.
 * Implements a thread-safe sliding window algorithm.
 */
object RequestRateLimiter {
    // Gemini free tier generally allows 15 RPM. Enforcing 10 RPM client-side
    // ensures we always stay safe and never hit 429 Rate Limit errors.
    private const val MAX_REQUESTS = 10
    private const val WINDOW_MILLIS = 60000L // 1 minute window

    private val requestTimestamps = ConcurrentLinkedQueue<Long>()

    /**
     * Checks if a request is allowed under the rate limit.
     * If allowed, records the request timestamp and returns true.
     * Otherwise, returns false.
     */
    @Synchronized
    fun isRequestAllowed(): Boolean {
        val currentTime = System.currentTimeMillis()
        
        // Evict expired timestamps outside of the 1-minute sliding window
        while (requestTimestamps.isNotEmpty() && (currentTime - requestTimestamps.peek()!! > WINDOW_MILLIS)) {
            requestTimestamps.poll()
        }

        return if (requestTimestamps.size < MAX_REQUESTS) {
            requestTimestamps.add(currentTime)
            true
        } else {
            false
        }
    }

    /**
     * Calculates the estimated wait time in seconds before a new request slot becomes available.
     */
    @Synchronized
    fun getSecondsToWait(): Int {
        val currentTime = System.currentTimeMillis()
        if (requestTimestamps.isEmpty()) return 0
        val oldestInWindow = requestTimestamps.peek() ?: return 0
        val elapsed = currentTime - oldestInWindow
        val remainingMillis = WINDOW_MILLIS - elapsed
        return if (remainingMillis > 0) {
            (remainingMillis / 1000).toInt().coerceAtLeast(1)
        } else {
            0
        }
    }
}
