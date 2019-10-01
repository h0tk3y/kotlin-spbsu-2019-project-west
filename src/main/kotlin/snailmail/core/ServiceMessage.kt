package snailmail.core

import java.util.*

abstract class ServiceMessage(id: UUID, date: Date) : Message(id, date = date) {
    abstract fun getText(): String
}