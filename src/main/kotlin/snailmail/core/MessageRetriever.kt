package snailmail.core

import java.util.*

interface MessageRetriever {
    fun getMessages(chat: Chat): List<Message>
    fun getMessagesSince(chat: Chat, since: Date): List<Message>
}