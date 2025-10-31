package com.tracelens.model

import java.time.Instant

/**
 * Represents a single log entry captured during a user session
 */
data class LogEntry(
    val timestamp: Instant,
    val level: String,
    val logger: String,
    val message: String,
    val threadName: String,
    val throwable: String? = null
) {
    /**
     * Converts the log entry to SSE-compatible format
     */
    fun toSseFormat(): String {
        val baseLog = "[$timestamp] [$level] [$threadName] $logger - $message"
        return if (throwable != null) {
            "$baseLog\n$throwable"
        } else {
            baseLog
        }
    }
}
