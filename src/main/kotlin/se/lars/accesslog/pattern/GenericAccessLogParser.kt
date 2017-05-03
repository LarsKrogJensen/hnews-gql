package se.lars.accesslog.pattern

fun matchPatterns(vararg patterns: String, creator: (String) -> AccessLogElement): AccessLogParser {
    return object : AccessLogParser {
        override fun matchPattern(rawPattern: String): ExtractedPosition? {
            for (pattern in patterns) {
                val index = rawPattern.indexOf(pattern)
                if (index >= 0) {
                    val logElement = creator(pattern)
                    return ExtractedPosition(index, pattern.length, logElement)
                }
            }

            return null
        }
    }
}



