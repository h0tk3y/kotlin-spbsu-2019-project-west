package snailmail.core

import java.util.*

interface MessageRetriever {
    fun getMessages(): List<Message>
    fun getMessagesSince(since: Date): List<Message>
}