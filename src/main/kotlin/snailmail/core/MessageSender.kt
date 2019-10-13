package snailmail.core

interface MessageSender {
    fun sendTextMessage(content: String, chat: Chat)
}