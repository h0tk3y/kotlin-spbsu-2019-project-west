package snailmail.core

import java.util.*

abstract class ServiceMessage(id: UUID, date: Date) : Message(id, date = date) {
    abstract fun getText(): String
}

class UserJoinedMessage(id: UUID, date: Date, val user: UUID) : ServiceMessage(id, date) {
    override fun getText(): String = "User with ID $user joined this chat"
}

class UserLeftMessage(id: UUID, date: Date, val user: UUID) : ServiceMessage(id, date) {
    override fun getText(): String = "User with ID $user left this chat"
}