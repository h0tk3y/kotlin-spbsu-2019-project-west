package snailmail.server.data

import snailmail.core.*
import java.util.*

class LocalDataBase : DataBase {
    private var userCredentials = HashMap<String, String>()
    private var chats = mutableListOf<Chat>()
    private var userByUsername = HashMap<String, User>()
    private var userById = HashMap<UUID, User>()
    private var messagesByChatId = HashMap<UUID, MutableList<Message>>()
    private var chatByChatId = HashMap<UUID, Chat>()

    override fun verifyUserCredentials(username: String, password: String): Boolean {
        return (userCredentials.contains(username) && userCredentials[username] == password)
    }

    override fun getUserByUsername(username : String): User? {
        return userByUsername[username]
    }

    override fun getUserById(id : UUID): User? {
        return userById[id]
    }

    override fun getChats(userId : UUID): List<Chat> {
        return this.chats.filter {
            it.hasMember(userId)
        }
    }

    override fun getChatByChatId(id : UUID): Chat? {
        return chatByChatId[id]
    }

    override fun getPersonalChatWith(thisUser: UUID, otherUser: UUID): PersonalChat? {
        return chats.find {
            when (it) {
                is PersonalChat -> it.hasMember(thisUser)
                        && it.hasMember(otherUser)
                else -> false
            }
        } as PersonalChat?
    }

    override fun getMessagesByChatId(id : UUID): List<Message>? {
        return messagesByChatId[id]
    }

    override fun addUserCredentials(username: String, password: String) {
        userCredentials[username] = password
    }

    override fun addUser(user : User) {
        userById[user.id] = user
        userByUsername[user.username] = user
    }

    override fun addChat(chat : Chat) {
        chats.add(chat)
        messagesByChatId[chat.id] = mutableListOf()
        chatByChatId[chat.id] = chat
    }

    override fun addMessage(chat: UUID, message: Message) {
        messagesByChatId[chat]!!.add(message)
    }

    override fun deleteChat(id : UUID) {

    }

    override fun findUserById(id : UUID) : Boolean {
        return userById.containsKey(id)
    }

    override fun findUsername(username: String) : Boolean {
        return userCredentials.containsKey(username)
    }

    override fun findMessagesByChatId(chat: UUID): Boolean {
        return messagesByChatId.containsKey(chat)
    }

    override fun changeBannedContactOfUser(userId: UUID, targetUserId: UUID, isBanned: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun changeContactDisplayName(userId: UUID, targetUserId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun changePassword(credentials: UserCredentials): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteMessage(messageId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun editTextMessage(messageId: UUID, text: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findGroupChatById(chat: UUID): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findMessageById(messageId: UUID): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContactOfUser(userId: UUID, contactUserId: UUID): Contact? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGroupChatIdByTag(tag: String): UUID? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGroupChatPreferencesByChatId(chatId: UUID): GroupChatPreferences? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMembersOfChat(chatId: UUID): List<UUID>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMessageById(messageId: UUID): Message? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTextMessageById(messageId: UUID): TextMessage? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isMemberOfGroupChat(userId: UUID, chatId: UUID): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isOwnerOfGroupChat(userId: UUID, chatId: UUID): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun joinGroupChat(userId: UUID, chatId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeUserFromBlackListOfGroupChat(userId: UUID, chatId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeUserFromGroupChat(userId: UUID, chatId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setOwnerOfGroupChat(userId: UUID, chatId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setPreferredTiTleOfGroupChat(userId: UUID, chatId: UUID, title: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setPrivateInviteTokenOfGroupChat(inviteToken: String, chatId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setPublicTagOfGroupChat(publicTag: String, chatId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setTitleOfGroupChat(title: String, chatId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateProfileDisplayName(userId: UUID, displayName: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateProfileEmail(userId: UUID, email: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun withdrawPrivateInviteTokenOfGroupChat(chatId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun withdrawPublicTagOfGroupChat(chatId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}