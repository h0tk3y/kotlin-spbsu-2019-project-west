package snailmail.core

import java.util.*

abstract class Message(
        val id: UUID,
        val sender: UUID? = null,
        val date: Date,
        val seen: Boolean = false
)