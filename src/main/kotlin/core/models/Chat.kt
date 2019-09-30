package core.models

import java.util.*

abstract class Chat {
    val ID: UUID? = TODO()

    abstract fun getChatHistory(): MessageSupplier
}