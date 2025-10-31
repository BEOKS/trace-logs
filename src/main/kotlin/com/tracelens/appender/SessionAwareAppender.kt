package com.tracelens.appender

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.ThrowableProxyUtil
import ch.qos.logback.core.AppenderBase
import com.tracelens.model.LogEntry
import com.tracelens.service.LogBufferService
import org.slf4j.MDC
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import java.time.Instant

/**
 * Custom Logback appender that captures logs for specific sessions
 */
class SessionAwareAppender : AppenderBase<ILoggingEvent>(), ApplicationContextAware {

    companion object {
        const val SESSION_ID_KEY = "tracelens.sessionId"
    }

    private var applicationContext: ApplicationContext? = null
    private var logBufferService: LogBufferService? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    override fun start() {
        super.start()
        // Lazy initialization of LogBufferService
        try {
            logBufferService = applicationContext?.getBean(LogBufferService::class.java)
        } catch (e: Exception) {
            addWarn("Could not get LogBufferService bean, session logging will be disabled", e)
        }
    }

    override fun append(eventObject: ILoggingEvent) {
        // Get session ID from MDC
        val sessionId = MDC.get(SESSION_ID_KEY) ?: return

        val service = logBufferService ?: return

        try {
            val logEntry = LogEntry(
                timestamp = Instant.ofEpochMilli(eventObject.timeStamp),
                level = eventObject.level.toString(),
                logger = eventObject.loggerName,
                message = eventObject.formattedMessage,
                threadName = eventObject.threadName,
                throwable = eventObject.throwableProxy?.let {
                    ThrowableProxyUtil.asString(it)
                }
            )

            service.addLog(sessionId, logEntry)
        } catch (e: Exception) {
            addError("Error appending log to session buffer", e)
        }
    }
}
