package snailmail.server.data

import snailmail.core.*
import java.awt.DisplayMode
import java.util.*

interface DataBase {
    fun verifyUserCredentials(username : String, password : String) : Boolean

    fun getUserByUsername(username : String): User?

    fun getUserById(id : UUID): User?

    fun getChats(userId: UUID): List<Chat>?

    fun getChatByChatId(id : UUID): Chat?

    fun getPersonalChatWith(thisUser: UUID, otherUser: UUID): PersonalChat?

    fun getMessagesByChatId(id : UUID): List<Message>?

    fun addUserCredentials(username: String, password: String)

    fun getMembersOfChat(chatId: UUID) : List<UUID>?

    fun addUser(user : User)

    fun addChat(chat : Chat)

    fun addMessage(chat: UUID, message: Message)

    fun deleteChat(id : UUID)

    fun findUserById(id : UUID) : Boolean

    fun findUsername(username: String) : Boolean

    fun findMessagesByChatId(chat: UUID) : Boolean

    fun findMessageById(messageId: UUID) : Boolean

    fun findGroupChatById(chat: UUID) : Boolean

    fun changePassword(credentials: UserCredentials) : Boolean

    fun getGroupChatIdByTag(tag: String) : UUID?

    fun getTextMessage(messageId: UUID) : TextMessage?

    fun joinGroupChat(userId: UUID, chatId: UUID)

    fun isMemberOfGroupChat(userId: UUID, chatId: UUID) : Boolean

    fun isOwnerOfGroupChat(userId: UUID, chatId: UUID) : Boolean

    fun removeUserFromGroupChat(userId: UUID, chatId: UUID)

    fun removeUserFromBlackListOfGroupChat(userId: UUID, chatId: UUID)

    fun setOwnerOfGroupChat(userId: UUID, chatId: UUID)

    fun setTitleOfGroupChat(title: String, chatId: UUID)

    fun setPublicTagOfGroupChat(publicTag: String, chatId: UUID)

    fun setPrivateInviteTokenOfGroupChat(inviteToken: String, chatId: UUID)

    fun withdrawPublicTagOfGroupChat(chatId: UUID)

    fun withdrawPrivateInviteTokenOfGroupChat(chatId: UUID)

    fun setPreferredTiTleOfGroupChat(userId: UUID, chatId: UUID, title: String)

    fun getGroupChatPreferencesByChatId(userId: UUID, chatId: UUID) : GroupChatPreferences?

    fun getMessageById(messageId: UUID) : Message?

    fun editTextMessage(messageId: UUID, text: String)

    fun deleteMessage(messageId: UUID)

    fun getContactOfUser(userId: UUID, contactUserId: UUID) : Contact?

    fun changeContactDisplayName(userId: UUID, targetUserId: UUID, newDisplayName: String)

    fun changeBannedContactOfUser(userId: UUID, targetUserId: UUID, isBanned: Boolean)

    fun updateProfileDisplayName(userId: UUID, displayName: String)

    fun updateProfileEmail(userId: UUID, email: String?)

    fun addUserToBlacklistOfGroupChat(userId: UUID, chatId: UUID)

    fun addToDeletedMessages(messageId: UUID)
}