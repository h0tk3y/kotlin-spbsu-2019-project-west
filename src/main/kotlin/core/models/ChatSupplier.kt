package core.models

interface ChatSupplier {
    fun getChats(): List<Chat>
}