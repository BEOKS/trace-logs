package com.tracelens.extractor

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory

/**
 * Extracts session ID from HTTP header
 */
class HeaderBasedSessionIdExtractor(
    private val headerName: String
) : SessionIdExtractor {

    private val logger = LoggerFactory.getLogger(HeaderBasedSessionIdExtractor::class.java)

    override fun extractSessionId(request: HttpServletRequest): String? {
        val sessionId = request.getHeader(headerName)
        if (sessionId != null) {
            logger.trace("Extracted session ID from header '{}': {}", headerName, sessionId)
        }
        return sessionId
    }
}
