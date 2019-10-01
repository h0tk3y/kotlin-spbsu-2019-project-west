package snailmail.core

import java.util.*

class UserLeftMessage(id: UUID, date: Date, val user: UUID) : ServiceMessage(id, date) {
    override fun getText(): String = "User with ID $user left this chat"
}