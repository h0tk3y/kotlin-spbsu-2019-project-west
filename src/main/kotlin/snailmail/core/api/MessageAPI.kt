package snailmail.core.api

import snailmail.core.Chat
import snailmail.core.MessageRetriever
import snailmail.core.TextMessage
import java.util.*

interface MessageAPI {
    fun getChatMessages(token: AuthToken, chat: UUID): MessageRetriever

    fun sendTextMessage(token: AuthToken, text: String, chat: UUID): TextMessage
}