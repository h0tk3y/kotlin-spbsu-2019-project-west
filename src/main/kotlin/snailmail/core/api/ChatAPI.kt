package snailmail.core.api

import snailmail.core.ChatRetriever
import snailmail.core.PersonalChat
import snailmail.core.User

interface ChatAPI {
    fun getAvailableChats(): ChatRetriever

    fun getPersonalChatWith(user: User): PersonalChat
}