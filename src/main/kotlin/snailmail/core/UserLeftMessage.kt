package snailmail.core

import java.util.*

class UserLeftMessage(id: UUID, chatId: UUID, date: Date, val user: UUID) : ServiceMessage(id, chatId, date) {
    override fun getText(): String = "User with ID $user left this chat"
}