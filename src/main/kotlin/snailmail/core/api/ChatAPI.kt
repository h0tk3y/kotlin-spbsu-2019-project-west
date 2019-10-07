package snailmail.core.api

import snailmail.core.ChatRetriever
import snailmail.core.PersonalChat
import snailmail.core.User

interface ChatAPI {
    fun getAvailableChats(token: AuthToken): ChatRetriever

    fun getPersonalChatWith(token: AuthToken, user: User): PersonalChat
}