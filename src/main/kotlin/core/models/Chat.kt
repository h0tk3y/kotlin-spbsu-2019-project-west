package core.models

import java.util.*

abstract class Chat(val ID: UUID) {
    abstract fun getChatHistory(): MessageSupplier
}