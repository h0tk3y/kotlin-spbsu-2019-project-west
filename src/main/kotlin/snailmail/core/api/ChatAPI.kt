package snailmail.core.api

import snailmail.core.*
import java.util.*

interface ChatAPI {
    fun getAvailableChats(token: AuthToken): List<Chat>

    fun getPersonalChatWith(token: AuthToken, user: UUID): PersonalChat
    fun createGroupChat(token: AuthToken, title: String, invitedMembers: List<UUID>): GroupChat
}