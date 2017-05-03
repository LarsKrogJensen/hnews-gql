package se.lars.accesslog.pattern

import io.vertx.ext.web.impl.Utils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DateTimeElement(private val dateFormat: DateFormat) : AccessLogElement {

    override fun formattedValue(values: LogData) = this.dateFormat.format(values.startMillis)

    companion object {
        fun matcher(): AccessLogParser {
            return object : AccessLogParser {
                override fun matchPattern(rawPattern: String): ExtractedPosition? {
                    // Check if we have a configured datetime element
                    var index = rawPattern.indexOf("%{")

                    if (index >= 0) {
                        val indexEndConfigurationDatetime = rawPattern.indexOf("}t")
                        val indexEndConfiguration = rawPattern.indexOf("}")

                        if (indexEndConfigurationDatetime > index && indexEndConfigurationDatetime == indexEndConfiguration) {
                            val configurationString = rawPattern.substring(index + 2, indexEndConfigurationDatetime)

                            return ExtractedPosition(index, configurationString.length + 4, DateTimeElement(deriveDateFormatFromConfigurationString(configurationString)))
                        }
                    }

                    // Check if we have an unconfigured element
                    val requestPattern = "%t"
                    index = rawPattern.indexOf(requestPattern)

                    if (index >= 0) {
                        return ExtractedPosition(index, requestPattern.length, DateTimeElement(Utils.createRFC1123DateTimeFormatter()))
                    }

                    return null
                }

                protected fun deriveDateFormatFromConfigurationString(configurationString: String?): DateFormat {

                    if (configurationString != null && configurationString.isNotEmpty()) {

                        val configurationTokens = configurationString.split("\\|".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()

                        if (configurationTokens.size == 3) {
                            // Assume that is a configuration including format, timezone and locale
                            val dtf = SimpleDateFormat(configurationTokens[0], Locale.forLanguageTag(configurationTokens[2]))
                            dtf.timeZone = TimeZone.getTimeZone(configurationTokens[1])

                            return dtf
                        } else {
                            // Assume this is just a format configuration
                            val dtf = SimpleDateFormat(configurationTokens[0], Locale.ENGLISH)
                            dtf.timeZone = TimeZone.getTimeZone("GMT")
                        }
                    }

                    return Utils.createRFC1123DateTimeFormatter()
                }

            }
        }
    }
}
