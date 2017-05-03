package se.lars.accesslog.pattern


interface AccessLogElement {
    fun formattedValue(values: LogData): String
}

@FunctionalInterface
interface AccessLogParser {
    fun matchPattern(rawPattern: String): ExtractedPosition?
}