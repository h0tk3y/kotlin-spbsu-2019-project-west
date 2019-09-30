package core.models

abstract class GroupChat : Chat() {
    abstract fun getAdministrators(): UserSupplier
    abstract fun getMembers(): UserSupplier
}