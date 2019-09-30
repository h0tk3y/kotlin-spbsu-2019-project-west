package core.api

import core.models.Message
import core.models.MessageSupplier
import java.util.*

interface MessageAPI {
    fun sendMessage(message: Message, chatID: UUID)
    fun getChatHistory(chatID: UUID): MessageSupplier
    fun editMessage(messageID: UUID, editedMessage: Message)
    fun deleteMessage(messageID: UUID)
}