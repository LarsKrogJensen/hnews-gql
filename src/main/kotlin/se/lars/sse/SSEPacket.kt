package se.lars.sse

import io.vertx.core.buffer.Buffer

internal class SSEPacket {

    private var payload: StringBuilder = StringBuilder()

    fun append(buffer: Buffer): List<Event> {
        payload.append(buffer.toString())

        val events = mutableListOf<Event>()
        parseEvents(events)
        return events
    }


    private fun parseEvents(events: MutableList<Event>) {
        val event = parseEvent()
        if (event != null) {
            events += event
            parseEvents(events)
        }
    }

    private fun parseEvent(): Event? {
        val endOfPacketIndex = payload.indexOf(END_OF_PACKET)
        if (endOfPacketIndex == -1)
            return null
        val packet = payload.substring(0, endOfPacketIndex)
        payload.delete(0, endOfPacketIndex + END_OF_PACKET.length)

        return parsePacket(packet)
    }

    private fun parsePacket(packet: CharSequence): Event {
        var id: String? = null
        var data: String? = null
        var retry: Int? = null
        var event: String? = null

        val lines = packet.split(LINE_SEPARATOR)
        for (line in lines) {
            val idx = line.indexOf(FIELD_SEPARATOR)
            if (idx == -1 || idx == 0) {
                continue // ignore line
            }

            val name = line.substring(0, idx)
            val value = line.substring(idx + 1, line.length).trim()

            when (name) {
                "id"    -> id = value
                "data"  -> data = value
                "event" -> event = value
                "retry" -> retry = value.toInt()
            }
        }

        return Event(event, data, id, retry)
    }

    override fun toString(): String {
        return payload.toString()
    }

    companion object {

        /* Use constants, but hope this will never change in the future (it should'nt) */
        private val END_OF_PACKET = "\n\n"
        private val LINE_SEPARATOR = "\n"
        private val FIELD_SEPARATOR = ":"
    }
}