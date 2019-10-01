package snailmail.core

import java.util.*

interface ChatRetriever {
    fun getChats(user: UUID): List<Chat>
}