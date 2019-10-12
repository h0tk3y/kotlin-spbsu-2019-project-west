package snailmail.core

import java.util.*

interface ChatRetriever {
    fun getChats(): List<Chat>
}

class ListChatRetriever(private val chats: List<Chat>) : ChatRetriever {
    override fun getChats(): List<Chat> = chats
}