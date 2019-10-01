package snailmail.core

import java.util.*

class TextMessage(
        id: UUID,
        sender: UUID,
        date: Date,
        seen: Boolean = false,
        val content: String
) : Message(id, sender, date, seen) {
    override fun toString(): String {
        return "at $date: $content"
    }
}