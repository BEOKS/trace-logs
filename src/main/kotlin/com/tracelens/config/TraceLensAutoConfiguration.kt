package com.tracelens.config

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import com.tracelens.appender.SessionAwareAppender
import com.tracelens.controller.LogStreamController
import com.tracelens.filter.SessionTrackingFilter
import com.tracelens.service.LogBufferService
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Auto-configuration for TraceLens
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnProperty(
    prefix = "trace-lens",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(TraceLensProperties::class)
@EnableScheduling
class TraceLensAutoConfiguration(
    private val properties: TraceLensProperties,
    private val applicationContext: ApplicationContext
) {
    private val logger = LoggerFactory.getLogger(TraceLensAutoConfiguration::class.java)

    @PostConstruct
    fun init() {
        logger.info("TraceLens is enabled")
        logger.info("TraceLens endpoint: {}/stream", properties.endpointPath)
        logger.info("TraceLens session tracking: {}",
            properties.sessionHeaderName ?: "JSESSIONID (default)")

        // Configure the SessionAwareAppender
        configureLogbackAppender()
    }

    @Bean
    fun logBufferService(): LogBufferService {
        return LogBufferService(properties)
    }

    @Bean
    fun logStreamController(logBufferService: LogBufferService): LogStreamController {
        return LogStreamController(logBufferService, properties)
    }

    @Bean
    fun sessionTrackingFilter(): FilterRegistrationBean<SessionTrackingFilter> {
        val filter = SessionTrackingFilter(properties)
        val registration = FilterRegistrationBean(filter)

        registration.order = properties.filterOrder
        registration.urlPatterns = properties.includePatterns

        logger.info("Registered SessionTrackingFilter with order: {}", properties.filterOrder)

        return registration
    }

    /**
     * Configures the Logback appender to capture logs
     */
    private fun configureLogbackAppender() {
        val loggerContext = LoggerFactory.getILoggerFactory() as? LoggerContext

        if (loggerContext == null) {
            logger.warn("Could not get LoggerContext, SessionAwareAppender will not be configured")
            return
        }

        // Create and configure the appender
        val appender = SessionAwareAppender()
        appender.context = loggerContext
        appender.name = "TraceLensAppender"
        appender.setApplicationContext(applicationContext)
        appender.start()

        // Attach to root logger
        val rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)
        rootLogger.addAppender(appender)

        logger.info("SessionAwareAppender configured and attached to root logger")
    }
}
