package snailmail.core.api

import snailmail.core.Message
import snailmail.core.TextMessage
import java.util.*

interface MessageAPI {
    fun getChatMessages(token: AuthToken, chat: UUID): List<Message>

    fun sendTextMessage(token: AuthToken, text: String, chat: UUID): TextMessage
}