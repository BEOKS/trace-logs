package com.tracelens.service

import com.tracelens.config.TraceLensProperties
import com.tracelens.model.LogEntry
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Service that manages session-specific log buffers
 */
@Service
class LogBufferService(
    private val properties: TraceLensProperties
) {
    private val logger = LoggerFactory.getLogger(LogBufferService::class.java)

    // Map of session ID to SessionLogBuffer
    private val sessionBuffers = ConcurrentHashMap<String, SessionLogBuffer>()

    /**
     * Adds a log entry to the session's buffer
     */
    fun addLog(sessionId: String, logEntry: LogEntry) {
        val buffer = sessionBuffers.computeIfAbsent(sessionId) {
            SessionLogBuffer(properties.maxBufferSize)
        }
        buffer.add(logEntry)
    }

    /**
     * Gets all logs for a session and clears the buffer
     */
    fun getAndClearLogs(sessionId: String): List<LogEntry> {
        return sessionBuffers[sessionId]?.getAndClear() ?: emptyList()
    }

    /**
     * Gets all logs for a session without clearing
     */
    fun getLogs(sessionId: String): List<LogEntry> {
        return sessionBuffers[sessionId]?.get() ?: emptyList()
    }

    /**
     * Removes a session's log buffer
     */
    fun removeSession(sessionId: String) {
        sessionBuffers.remove(sessionId)
    }

    /**
     * Cleans up old session buffers based on TTL
     */
    @Scheduled(fixedDelayString = "\${trace-lens.cleanup-interval:60000}")
    fun cleanupOldBuffers() {
        val now = Instant.now()
        val ttlSeconds = properties.bufferTtlMinutes * 60

        val sessionsToRemove = sessionBuffers.entries
            .filter { (_, buffer) ->
                val lastAccessSeconds = now.epochSecond - buffer.lastAccess.epochSecond
                lastAccessSeconds > ttlSeconds
            }
            .map { it.key }

        sessionsToRemove.forEach { sessionId ->
            sessionBuffers.remove(sessionId)
            logger.debug("Removed expired log buffer for session: {}", sessionId)
        }

        if (sessionsToRemove.isNotEmpty()) {
            logger.info("Cleaned up {} expired session log buffers", sessionsToRemove.size)
        }
    }

    /**
     * Gets the number of active session buffers
     */
    fun getActiveSessionCount(): Int = sessionBuffers.size

    /**
     * Internal class to manage a single session's log buffer
     */
    private class SessionLogBuffer(private val maxSize: Int) {
        private val logs = ConcurrentLinkedQueue<LogEntry>()
        private val lock = ReentrantReadWriteLock()

        @Volatile
        var lastAccess: Instant = Instant.now()
            private set

        fun add(logEntry: LogEntry) {
            lock.write {
                // Remove oldest entry if buffer is full
                while (logs.size >= maxSize) {
                    logs.poll()
                }
                logs.offer(logEntry)
                lastAccess = Instant.now()
            }
        }

        fun get(): List<LogEntry> {
            return lock.read {
                lastAccess = Instant.now()
                logs.toList()
            }
        }

        fun getAndClear(): List<LogEntry> {
            return lock.write {
                lastAccess = Instant.now()
                val result = logs.toList()
                logs.clear()
                result
            }
        }
    }
}
