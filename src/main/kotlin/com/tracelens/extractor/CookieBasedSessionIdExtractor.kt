package com.tracelens.extractor

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory

/**
 * Extracts session ID from HTTP cookie
 */
class CookieBasedSessionIdExtractor(
    private val cookieName: String
) : SessionIdExtractor {

    private val logger = LoggerFactory.getLogger(CookieBasedSessionIdExtractor::class.java)

    override fun extractSessionId(request: HttpServletRequest): String? {
        val sessionId = request.cookies?.firstOrNull { it.name == cookieName }?.value
        if (sessionId != null) {
            logger.trace("Extracted session ID from cookie '{}': {}", cookieName, sessionId)
        }
        return sessionId
    }
}
