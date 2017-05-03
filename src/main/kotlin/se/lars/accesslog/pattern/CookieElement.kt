package se.lars.accesslog.pattern

class CookieElement(private val identifier: String = "") : AccessLogElement {

    override fun formattedValue(values: LogData) = values.cookies
            .asSequence()
            .filter { c -> this.identifier == c.name }
            .firstOrNull()?.value ?: "-"

    companion object {
        fun matcher() = object : AccessLogParser {
            override fun matchPattern(rawPattern: String): ExtractedPosition? {
                val index = rawPattern.indexOf("%{")

                if (index >= 0) {
                    val indexEndConfiguration = rawPattern.indexOf("}c")
                    if (indexEndConfiguration > index) {
                        val configurationString = rawPattern.substring(index + 2, indexEndConfiguration)
                        return ExtractedPosition(index, configurationString.length + 4, CookieElement(configurationString))
                    }
                }

                return null
            }
        }
    }


}
