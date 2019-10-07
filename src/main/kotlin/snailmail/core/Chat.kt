package snailmail.core

import java.util.*

abstract class Chat(val id: UUID) {
    abstract fun hasMember(user: UUID): Boolean
}