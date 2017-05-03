package se.lars.accesslog.pattern

class PatternResolver {

    fun resolvePattern(rawPattern: String): ResolvedPatternResult {
        var rawPatternInEvaluation = rawPattern
        val sbEvaluatedPattern = StringBuilder()
        val logElements = mutableListOf<AccessLogElement>()

        while (rawPatternInEvaluation.isNotEmpty()) {
            var bestStart = -1
            var bestOffset = 0
            var bestElement: AccessLogElement? = null

            // run the remaining raw pattern through all parsers
            // and find the best (with lowest start index)
            for (parser in parsers) {
                val extractedPosition = parser.matchPattern(rawPatternInEvaluation)

                // verify if the  extracted element is found and has a lower start index
                // the it is our current best match
                if (extractedPosition != null && (extractedPosition.start  < bestStart || bestStart == -1)) {
                    bestStart = extractedPosition.start
                    bestOffset = extractedPosition.offset
                    bestElement = extractedPosition.element
                }
            }

            // Do we have a match ?
            if (bestStart > -1) {
                if (bestStart > 0) {
                    // We need to take over some untranslatable part first
                    sbEvaluatedPattern.append(rawPatternInEvaluation.substring(0, bestStart))
                }

                // Shorten the raw pattern till where we found replacement
                rawPatternInEvaluation = rawPatternInEvaluation.substring(bestStart + bestOffset)

                //System.out.println("|" + rawPatternInEvaluation + "|");

                // Add the placeholder - for now always type string
                sbEvaluatedPattern.append("%s")

                // Add the log element
                if (bestElement != null)
                    logElements.add(bestElement)

            } else {
                // Looks like no more that can be resolved
                if (rawPatternInEvaluation.isNotEmpty()) {
                    sbEvaluatedPattern.append(rawPatternInEvaluation)
                }

                break
            }
        }

        return ResolvedPatternResult(sbEvaluatedPattern.toString(), logElements)
    }

    companion object {

        private val parsers: List<AccessLogParser> =
                listOf(RequestElement.matcher(),
                        StatusElement.matcher(),
                        DateTimeElement.matcher(),
                        MethodElement.matcher(),
                        VersionElement.matcher(),
                        HeaderElement.matcher(),
                        DurationElement.matcher(),
                        BytesSentElement.matcher(),
                        HostElement.matcher(),
                        CookieElement.matcher())
    }
}
