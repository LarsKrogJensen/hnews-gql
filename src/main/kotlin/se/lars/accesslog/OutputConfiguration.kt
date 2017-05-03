package se.lars.accesslog

import io.vertx.core.logging.Logger
import se.lars.accesslog.pattern.AccessLogElement
import se.lars.accesslog.pattern.LogData

class OutputConfiguration(private val formatterPattern: String,
                          private val accessLogElements: List<AccessLogElement>,
                          private val loggers: List<Logger>) {

    fun doLog(values: LogData) {
        val parameterValues = parameterValues(values)
        val formattedString = String.format(formatterPattern, *parameterValues)

        for (logger in loggers) {
            logger.info(formattedString)
        }
    }

    private fun parameterValues(values: LogData): Array<String> {

        return accessLogElements
                .asSequence()
                .map { it.formattedValue(values) }
                .toMutableList()
                .toTypedArray()
    }

}