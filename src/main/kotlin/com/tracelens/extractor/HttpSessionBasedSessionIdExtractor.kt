package com.tracelens.extractor

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory

/**
 * Extracts session ID from HTTP session (JSESSIONID)
 */
class HttpSessionBasedSessionIdExtractor : SessionIdExtractor {

    private val logger = LoggerFactory.getLogger(HttpSessionBasedSessionIdExtractor::class.java)

    override fun extractSessionId(request: HttpServletRequest): String? {
        return try {
            val sessionId = request.getSession(false)?.id
            if (sessionId != null) {
                logger.trace("Extracted session ID from HTTP session: {}", sessionId)
            }
            sessionId
        } catch (e: Exception) {
            logger.warn("Failed to extract session ID from HTTP session", e)
            null
        }
    }
}
