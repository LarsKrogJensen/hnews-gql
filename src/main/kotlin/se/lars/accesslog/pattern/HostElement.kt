package se.lars.accesslog.pattern

class HostElement private constructor(private val mode: Mode) : AccessLogElement {
    private enum class Mode {
        REMOTE_HOST,
        LOCAL_HOST,
        LOCAL_PORT
    }

    override fun formattedValue(values: LogData) = when (this.mode) {
        Mode.LOCAL_HOST  -> values.localHost
        Mode.LOCAL_PORT  -> values.localPort.toString()
        Mode.REMOTE_HOST -> values.remoteHost
        else             -> "-"
    }

    companion object {
        fun matcher() = matchPatterns("%p", "%v", "%h") { pattern ->
            val mode = when (pattern) {
                "%p" -> Mode.LOCAL_PORT
                "%v" -> Mode.LOCAL_HOST
                else -> Mode.REMOTE_HOST
            }
            HostElement(mode)
        }
    }
}
