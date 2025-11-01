package com.tracelens.filter

import com.tracelens.appender.SessionAwareAppender
import com.tracelens.config.TraceLensProperties
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filter that extracts session identifier and stores it in MDC for log tracking
 */
class SessionTrackingFilter(
    private val properties: TraceLensProperties
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val sessionId = extractSessionId(request)

        if (sessionId != null) {
            MDC.put(SessionAwareAppender.SESSION_ID_KEY, sessionId)
        }

        try {
            filterChain.doFilter(request, response)
        } finally {
            if (sessionId != null) {
                MDC.remove(SessionAwareAppender.SESSION_ID_KEY)
            }
        }
    }

    /**
     * Extracts session ID from request based on configuration
     * Priority order: header > cookie > HTTP session
     */
    private fun extractSessionId(request: HttpServletRequest): String? {
        // 1. Try custom header if configured
        val customHeader = properties.sessionHeaderName
        if (customHeader != null) {
            val headerValue = request.getHeader(customHeader)
            if (headerValue != null) {
                return headerValue
            }
        }

        // 2. Try custom cookie if configured
        val customCookie = properties.sessionCookieName
        if (customCookie != null) {
            val cookieValue = extractCookieValue(request, customCookie)
            if (cookieValue != null) {
                return cookieValue
            }
        }

        // 3. Fall back to HTTP session (JSESSIONID)
        return try {
            request.getSession(false)?.id
        } catch (e: Exception) {
            logger.debug("Could not get session ID from request", e)
            null
        }
    }

    /**
     * Extracts cookie value from request
     */
    private fun extractCookieValue(request: HttpServletRequest, cookieName: String): String? {
        return request.cookies?.firstOrNull { it.name == cookieName }?.value
    }
}
