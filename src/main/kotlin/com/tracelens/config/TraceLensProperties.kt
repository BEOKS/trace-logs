package com.tracelens.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for TraceLens
 */
@ConfigurationProperties(prefix = "trace-lens")
data class TraceLensProperties(
    /**
     * Enable or disable TraceLens
     */
    var enabled: Boolean = true,

    /**
     * Custom header name for session identification (e.g., "X-Session-ID")
     * Priority order: header > cookie > HTTP session
     */
    var sessionHeaderName: String? = null,

    /**
     * Custom cookie name for session identification (e.g., "MY_SESSION_ID")
     * Used if sessionHeaderName is not set or header is not present
     * If null, falls back to HTTP session (JSESSIONID)
     */
    var sessionCookieName: String? = null,

    /**
     * Maximum number of log entries to buffer per session
     */
    var maxBufferSize: Int = 1000,

    /**
     * Time to live for session buffers in minutes
     * Buffers older than this will be cleaned up
     */
    var bufferTtlMinutes: Long = 30,

    /**
     * Interval for cleaning up old buffers in milliseconds
     */
    var cleanupInterval: Long = 60000,

    /**
     * SSE timeout in milliseconds (default: 30 minutes)
     */
    var sseTimeoutMillis: Long = 1800000,

    /**
     * Polling interval for checking new logs in milliseconds
     */
    var pollIntervalMillis: Long = 500,

    /**
     * Base endpoint path for TraceLens endpoints
     */
    var endpointPath: String = "/trace-lens/logs",

    /**
     * Filter order (lower values have higher priority)
     */
    var filterOrder: Int = -100,

    /**
     * URL patterns to include for session tracking
     * Empty list means all patterns are included
     */
    var includePatterns: List<String> = listOf("/**"),

    /**
     * URL patterns to exclude from session tracking
     */
    var excludePatterns: List<String> = listOf(
        "/trace-lens/**",
        "/actuator/**",
        "/static/**",
        "/public/**",
        "/webjars/**",
        "/favicon.ico"
    )
)
