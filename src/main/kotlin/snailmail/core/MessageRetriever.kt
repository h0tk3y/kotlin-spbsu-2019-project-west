package snailmail.core

import java.util.*

interface MessageRetriever {
    fun getMessages(): List<Message>
    fun getMessagesSince(since: Date): List<Message>
}

class ListMessageRetriever(private val messages: List<Message>) : MessageRetriever {
    override fun getMessages(): List<Message> = messages

    override fun getMessagesSince(since: Date) =
            messages.filter { it.date >= since }
}