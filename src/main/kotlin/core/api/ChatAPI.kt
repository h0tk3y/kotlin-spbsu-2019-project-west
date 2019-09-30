package core.api

import core.models.Chat
import core.models.ChatSupplier
import core.models.GroupChat
import core.models.PersonalChat
import java.util.*

interface ChatAPI {
    fun getAvailableChats(): ChatSupplier
    fun startPersonalChat(recipientID: UUID): PersonalChat

    fun createGroupChat(title: String): GroupChat
    fun addMemberToGroupChat(chatID: UUID, userID: UUID)
    fun changeTitleOfGroupChat(chatID: UUID, title: String)
}