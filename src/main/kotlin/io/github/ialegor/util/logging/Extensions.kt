package io.github.ialegor.util.logging

import io.github.ialegor.util.time.format
import mu.KLogger
import mu.KotlinLogging
import java.time.Duration

fun Any.log(): KLogger = KotlinLogging.logger(this::class.java.name)

fun KLogger.measure(message: String): KLoggerMeasure {
    return KLoggerMeasure(this, message)
}

class KLoggerMeasure(
    private val log: KLogger,
    private val message: String,
) {
    fun <T> extract(extractor: () -> T): KLoggerExtractor<T> {
        return KLoggerExtractor(log, message, extractor)
    }
}

class KLoggerExtractor<T>(
    private val log: KLogger,
    private val message: String,
    private val extractor: () -> T,
) {
    fun get(): T {
        return summary(null)
    }

    fun summary(extractor: (T.() -> String)? = null): T {
        try {
            log.info { message }
            val start = System.nanoTime()
            val result = this.extractor()
            val end = System.nanoTime()

            val elapsed = Duration.ofNanos(end - start)
            log.info { listOfNotNull(message, extractor?.invoke(result), "at ${elapsed.format()}") }
            return result
        } catch (e: Exception) {
            log.warn(e) { "$message: failed at ${e.message}" }
            throw e
        }
    }
}