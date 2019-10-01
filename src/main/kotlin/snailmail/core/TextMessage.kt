package snailmail.core

import java.util.*

class TextMessage(
        id: UUID,
        sender: UUID,
        date: Date,
        seen: Boolean = false,
        content: String
) : Message(id, sender, date, seen)