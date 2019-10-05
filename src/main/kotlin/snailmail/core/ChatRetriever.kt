package snailmail.core

import java.util.*

interface ChatRetriever {
    fun getChats(): List<Chat>
}