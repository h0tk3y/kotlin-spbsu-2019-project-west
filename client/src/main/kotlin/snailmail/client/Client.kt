package snailmail.client

import snailmail.core.*
import snailmail.core.Api
import snailmail.core.AuthToken
import java.util.*

class Client(private val api: Api) {
    private var token: AuthToken? = null
    private var username: String? = null

    private fun acquireToken(): AuthToken =
            token ?: throw NotAuthenticatedException("Token doesn't exist")

    fun self(): User {
        return findUser(username ?: throw NotAuthenticatedException("You must login first"))
    }

    fun findUser(username: String): User {
        val authToken = acquireToken()
        return api.getUserByUsername(authToken, username)
    }

    fun findUserById(id: UUID): User {
        val authToken = acquireToken()
        return api.getUserById(authToken, id)
    }

    fun sendMessage(username: String, message: String): TextMessage {
        val authToken = acquireToken()
        val user = findUser(username)
        return api.sendTextMessage(authToken, message,
                api.getPersonalChatWith(authToken, user.id).id)
    }

    fun sendMessageToGroupChat(chatTitle: String, message: String): TextMessage {
        val authToken = acquireToken()
        val chat = findGroupChat(chatTitle)
        return api.sendTextMessage(authToken, message, chat.id)
    }

    fun findAvailableChats(): List<Chat> {
        val authToken = acquireToken()
        return api.getChats(authToken)
    }

    private fun findPersonalChatWith(username: String): PersonalChat {
        val user = findUser(username)
        val authToken = acquireToken()
        return api.getPersonalChatWith(authToken, user.id)
    }

    fun getPersonalChatHistory(username: String): List<Message> {
        val authToken = acquireToken()
        val chat = findPersonalChatWith(username)
        return api.getChatMessages(authToken, chat.id)
    }

    private fun findGroupChat(chatTitle: String): GroupChat {
        val authToken = acquireToken()
        return api.getChats(authToken).filterIsInstance<GroupChat>().find { it.title == chatTitle }
                ?: throw ChatNotFoundException("This group chat doesn't exist!")
    }

    fun getGroupChatHistory(chatTitle: String): List<Message> {
        val authToken = acquireToken()
        val chat = findGroupChat(chatTitle)
        return api.getChatMessages(authToken, chat.id)
    }

    fun createGroupChat(chatTitle: String, members: List<String>): GroupChat {
        val authToken = acquireToken()
        val invitedMembers = members.map { findUser(it).id }
        return api.createGroupChat(authToken, chatTitle, invitedMembers)
    }

    fun authenticate(userCredentials: UserCredentials) {
        return processAuthentication(api.authenticate(userCredentials), userCredentials.username)
    }

    fun register(userCredentials: UserCredentials) {
        return processAuthentication(api.register(userCredentials), userCredentials.username)
    }

    fun changeUserCredentials(userCredentials: UserCredentials) {
        val authToken = acquireToken()
        return processAuthentication(api.changeCredentials(authToken, userCredentials), userCredentials.username)
    }

    private fun processAuthentication(authToken: AuthToken, username: String) {
        token = authToken
        this.username = username
    }
}


