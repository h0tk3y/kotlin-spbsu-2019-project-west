package snailmail.core.api

import snailmail.core.Chat
import snailmail.core.MessageRetriever
import snailmail.core.TextMessage

interface MessageAPI {
    fun getChatMessages(chat: Chat): MessageRetriever
    fun subscribeForNewMessages(): MessageRetriever

    fun sendTextMessage(text: String, chat: Chat): TextMessage
}