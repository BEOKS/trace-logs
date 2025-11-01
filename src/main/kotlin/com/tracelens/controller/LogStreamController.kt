package com.tracelens.controller

import com.tracelens.config.TraceLensProperties
import com.tracelens.extractor.SessionIdExtractor
import com.tracelens.service.LogBufferService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Controller that provides SSE endpoint for streaming logs
 */
@RestController
@RequestMapping("\${trace-lens.endpoint-path:/trace-lens/logs}")
class LogStreamController(
    private val logBufferService: LogBufferService,
    private val properties: TraceLensProperties,
    private val sessionIdExtractor: SessionIdExtractor
) {
    private val logger = LoggerFactory.getLogger(LogStreamController::class.java)
    private val executor = Executors.newScheduledThreadPool(10)

    /**
     * SSE endpoint for streaming logs in real-time
     */
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamLogs(request: HttpServletRequest): SseEmitter {
        val sessionId = extractSessionId(request)

        if (sessionId == null) {
            logger.warn("Cannot stream logs: no session ID found")
            val emitter = SseEmitter()
            emitter.completeWithError(IllegalStateException("No session ID found"))
            return emitter
        }

        logger.info("Starting log stream for session: {}", sessionId)

        val emitter = SseEmitter(properties.sseTimeoutMillis)

        // Send initial connection message
        try {
            emitter.send(
                SseEmitter.event()
                    .name("connected")
                    .data("TraceLens log streaming started for session: $sessionId")
            )
        } catch (e: Exception) {
            logger.error("Error sending initial message", e)
            emitter.completeWithError(e)
            return emitter
        }

        // Schedule periodic log polling
        val future = executor.scheduleAtFixedRate({
            try {
                val logs = logBufferService.getAndClearLogs(sessionId)

                if (logs.isNotEmpty()) {
                    logs.forEach { logEntry ->
                        emitter.send(
                            SseEmitter.event()
                                .name("log")
                                .data(logEntry.toSseFormat())
                        )
                    }
                } else {
                    // Send heartbeat to keep connection alive
                    emitter.send(
                        SseEmitter.event()
                            .name("heartbeat")
                            .data("")
                    )
                }
            } catch (e: Exception) {
                logger.debug("Error sending logs to SSE client", e)
                emitter.completeWithError(e)
            }
        }, 0, properties.pollIntervalMillis, TimeUnit.MILLISECONDS)

        // Handle emitter completion and timeout
        emitter.onCompletion {
            logger.info("Log stream completed for session: {}", sessionId)
            future.cancel(true)
        }

        emitter.onTimeout {
            logger.info("Log stream timed out for session: {}", sessionId)
            emitter.complete()
            future.cancel(true)
        }

        emitter.onError { throwable ->
            logger.error("Log stream error for session: {}", sessionId, throwable)
            future.cancel(true)
        }

        return emitter
    }

    /**
     * Endpoint to get all buffered logs at once (non-streaming)
     */
    @GetMapping("/snapshot")
    fun getLogSnapshot(request: HttpServletRequest): Map<String, Any> {
        val sessionId = extractSessionId(request)

        if (sessionId == null) {
            return mapOf(
                "error" to "No session ID found",
                "logs" to emptyList<String>()
            )
        }

        val logs = logBufferService.getLogs(sessionId)

        return mapOf(
            "sessionId" to sessionId,
            "count" to logs.size,
            "logs" to logs.map { it.toSseFormat() }
        )
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    fun health(): Map<String, Any> {
        return mapOf(
            "status" to "UP",
            "activeSessionBuffers" to logBufferService.getActiveSessionCount()
        )
    }

    /**
     * Extracts session ID from request using configured extractor
     */
    private fun extractSessionId(request: HttpServletRequest): String? {
        return sessionIdExtractor.extractSessionId(request)
    }
}
