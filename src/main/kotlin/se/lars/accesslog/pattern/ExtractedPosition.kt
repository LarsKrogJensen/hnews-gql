package se.lars.accesslog.pattern

data class ExtractedPosition(
        val start: Int,
        val offset: Int,
        val element: AccessLogElement
)