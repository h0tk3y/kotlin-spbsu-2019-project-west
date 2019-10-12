package snailmail.core

import java.util.*

abstract class ServiceMessage(id: UUID, chatId: UUID, date: Date) : Message("service", id, chatId, date = date) {
    abstract fun getText(): String
}