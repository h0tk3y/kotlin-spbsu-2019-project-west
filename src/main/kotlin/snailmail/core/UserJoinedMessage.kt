package snailmail.core

import java.util.*

class UserJoinedMessage(id: UUID, date: Date, val user: UUID) : ServiceMessage(id, date) {
    override fun getText(): String = "User with ID $user joined this chat"
}