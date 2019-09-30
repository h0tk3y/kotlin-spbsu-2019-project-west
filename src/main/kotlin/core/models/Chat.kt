package core.models

import java.util.*

abstract class Chat {
    val ID: UUID? = TODO()

    abstract fun getAdministrators(): UserSupplier
    abstract fun getMembers(): UserSupplier

    abstract fun getChatHistory(): MessageSupplier
}