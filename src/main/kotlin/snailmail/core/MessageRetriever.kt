package snailmail.core

import java.util.*

interface MessageRetriever {
    fun getMessages(chat: UUID): List<Message>
    fun getMessagesSince(chat: UUID, since: Date): List<Message> = getMessages(chat)
}