package snailmail.server.data

import snailmail.core.*
import java.util.*

interface DataBase {
    fun verifyUserCredentials(username : String, password : String) : Boolean

    fun getUserByUsername(username : String): User?

    fun getUserById(id : UUID): User?

    fun getChats(userId: UUID): List<Chat>

    fun getChatByChatId(id : UUID): Chat?

    fun getPersonalChatWith(thisUser: UUID, otherUser: UUID): PersonalChat?

    fun getMessagesByChatId(id : UUID): List<Message>?

    fun addUserCredentials(username: String, password: String)

    fun addUser(user : User)

    fun addChat(chat : Chat)

    fun addMessage(chat: UUID, message: Message)

    fun deleteChat(id : UUID)

    fun findUserById(id : UUID) : Boolean

    fun findUsername(username: String) : Boolean

    fun findMessagesByChatId(chat: UUID) : Boolean
}