package snailmail.client

import snailmail.core.*
import snailmail.core.api.*
import snailmail.server.Server
import java.util.*

class Client(private val api : API) {
    private var token : AuthToken? = null

    private fun acquireToken() : AuthToken =
        token ?: throw NoAuthTokenException("Token doesn't exist")

    fun findUser(username : String) : User {
        val authToken = acquireToken()
        return api.searchByUsername(authToken, username) ?:
        throw UserNotFoundException("This user doesn't exist")
    }

    fun sendMessage(username: String, message: String) : TextMessage {
        val authToken = acquireToken()
        val user = findUser(username)
        return api.sendTextMessage(authToken, message,
                api.getPersonalChatWith(authToken, user.id).id)
    }

    fun sendMessageToGroupChat(chatTitle: String, message: String) : TextMessage {
        val authToken = acquireToken()
        val chat = findGroupChat(chatTitle)
        return api.sendTextMessage(authToken, message, chat.id)
    }

    fun findAvailableChats() : List<Chat> {
        val authToken = acquireToken()
        return api.getAvailableChats(authToken).getChats()
    }

    private fun findPersonalChatWith(username: String) : PersonalChat {
        val user = findUser(username)
        val authToken = acquireToken()
        return api.getPersonalChatWith(authToken, user.id)
    }

    fun getPersonalChatHistory(username: String) : List<Message> {
        val authToken = acquireToken()
        val chat = findPersonalChatWith(username)
        return api.getChatMessages(authToken, chat.id).getMessages()
    }

    private fun findGroupChat(chatTitle: String) : GroupChat {
        val authToken = acquireToken()
        return api.getAvailableChats(authToken).getChats().filterIsInstance<GroupChat>().find { it.title == chatTitle }
                ?: throw ChatNotFoundException("This group chat doesn't exist!")
    }

    fun getGroupChatHistory(chatTitle : String) : List<Message> {
        val authToken = acquireToken()
        val chat = findGroupChat(chatTitle)
        return api.getChatMessages(authToken, chat.id).getMessages()
    }

    fun createGroupChat(chatTitle: String, members: List<String>) : GroupChat {
        val authToken = acquireToken()
        val invitedMembers = members.map { findUser(it).id }
        return api.createGroupChat(authToken, chatTitle, invitedMembers)
    }

    fun authenticate(userCredentials: UserCredentials) : Boolean {
        val authenticationResult = api.authenticate(userCredentials)
        if (authenticationResult is AuthSuccessful)
            token = authenticationResult.token
        return authenticationResult.successful
    }

    fun register(userCredentials: UserCredentials) : Boolean {
        val authenticationResult = api.register(userCredentials)
        if (authenticationResult is AuthSuccessful)
            token = authenticationResult.token
        return authenticationResult.successful
    }
}


