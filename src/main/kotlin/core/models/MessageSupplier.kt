package core.models

interface MessageSupplier {
    fun getMessages(): List<Message>
}