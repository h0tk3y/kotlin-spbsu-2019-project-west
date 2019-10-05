package snailmail.core

import java.util.*

class UserJoinedMessage(id: UUID, chatId: UUID, date: Date, val user: UUID) : ServiceMessage(id, chatId, date) {
    override fun getText(): String = "User with ID $user joined this chat"
}