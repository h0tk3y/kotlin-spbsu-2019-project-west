package snailmail.server.data

import snailmail.core.*
import snailmail.server.SimpleJwt
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
        } as PersonalChat
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

}