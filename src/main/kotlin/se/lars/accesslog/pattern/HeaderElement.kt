package se.lars.accesslog.pattern

class HeaderElement private constructor(private val mode: Mode, private val identifier: String) : AccessLogElement {
    private enum class Mode {
        INCOMING,
        OUTGOING
    }

    override fun formattedValue(values: LogData): String {
        val headers = if (Mode.INCOMING == this.mode) values.requestHeaders else values.responseHeaders

        if (headers.contains(identifier)) {
            return headers.get(identifier)
        } else {
            return "-"
        }
    }

    companion object {
        fun matcher() = object : AccessLogParser {
            override fun matchPattern(rawPattern: String): ExtractedPosition? {
                val index = rawPattern.indexOf("%{")

                if (index >= 0) {
                    val indexEndConfiguration = rawPattern.indexOf("}")

                    if (indexEndConfiguration > index
                            && rawPattern.length > indexEndConfiguration
                            && (rawPattern[indexEndConfiguration + 1] == 'i' || rawPattern[indexEndConfiguration + 1] == 'o')) {
                        val configurationString = rawPattern.substring(index + 2, indexEndConfiguration)

                        return ExtractedPosition(index, configurationString.length + 4, HeaderElement(if (rawPattern[indexEndConfiguration + 1] == 'i') Mode.INCOMING else Mode.OUTGOING, configurationString))
                    }
                }

                return null
            }
        }
    }
}
