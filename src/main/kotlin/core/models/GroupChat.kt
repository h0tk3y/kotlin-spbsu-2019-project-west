package core.models

import java.util.*

abstract class GroupChat(ID: UUID) : Chat(ID) {
    abstract fun getAdministrators(): UserSupplier
    abstract fun getMembers(): UserSupplier
}