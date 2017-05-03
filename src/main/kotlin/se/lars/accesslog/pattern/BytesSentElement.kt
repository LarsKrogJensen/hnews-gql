package se.lars.accesslog.pattern

class BytesSentElement private constructor(private val mode: Mode) : AccessLogElement {

    private enum class Mode {
        NO_BYTES_NULL,
        NO_BYTES_DASH
    }

    override fun formattedValue(values: LogData): String {
        if (values.bytesSent > 0) {
            return "" + values.bytesSent
        } else {
            return if (Mode.NO_BYTES_DASH == this.mode) "-" else "0"
        }
    }

    companion object {
        fun matcher() = matchPatterns("%b", "%B") { pattern ->
            val mode = if(pattern == "%b") Mode.NO_BYTES_DASH else Mode.NO_BYTES_NULL
            BytesSentElement(mode)
        }
    }
}
