package com.tracelens.extractor

import jakarta.servlet.http.HttpServletRequest

/**
 * Interface for extracting session ID from HttpServletRequest
 */
interface SessionIdExtractor {
    /**
     * Extracts session ID from the given request
     * @param request The HTTP servlet request
     * @return Session ID if found, null otherwise
     */
    fun extractSessionId(request: HttpServletRequest): String?
}
