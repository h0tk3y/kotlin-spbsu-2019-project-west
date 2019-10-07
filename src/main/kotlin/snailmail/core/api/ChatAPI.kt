package snailmail.core.api

import snailmail.core.ChatRetriever
import snailmail.core.GroupChat
import snailmail.core.PersonalChat
import snailmail.core.User
import java.util.*

interface ChatAPI {
    fun getAvailableChats(): ChatRetriever

    fun getPersonalChatWith(token: AuthToken, user: UUID): PersonalChat
    fun createGroupChat(token: AuthToken, title: String, invitedMembers: List<UUID>): GroupChat
}