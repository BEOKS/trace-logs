package com.tracelens.filter

import com.tracelens.appender.SessionAwareAppender
import com.tracelens.extractor.SessionIdExtractor
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filter that extracts session identifier and stores it in MDC for log tracking
 */
class SessionTrackingFilter(
    private val sessionIdExtractor: SessionIdExtractor
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val sessionId = sessionIdExtractor.extractSessionId(request)

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
}
