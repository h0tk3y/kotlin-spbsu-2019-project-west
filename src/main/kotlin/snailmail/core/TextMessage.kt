package snailmail.core

import java.util.*

class TextMessage(
        id: UUID,
        chatId: UUID,
        sender: UUID,
        date: Date,
        seen: Boolean = false,
        val content: String
) : Message("text", id, chatId, sender, date, seen) {
    override fun toString(): String {
        return "at $date: $content"
    }
}