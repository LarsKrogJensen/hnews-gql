package se.lars.accesslog.pattern


data class ResolvedPatternResult(
        val resolvedPattern: String,
        val logElements: List<AccessLogElement>
)
