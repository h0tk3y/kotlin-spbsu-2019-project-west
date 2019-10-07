package snailmail.core.api

import snailmail.core.Chat
import snailmail.core.MessageRetriever
import snailmail.core.TextMessage

interface MessageAPI {
    fun getChatMessages(token: AuthToken, chat: Chat): MessageRetriever

    fun sendTextMessage(token: AuthToken, text: String, chat: Chat): TextMessage
}