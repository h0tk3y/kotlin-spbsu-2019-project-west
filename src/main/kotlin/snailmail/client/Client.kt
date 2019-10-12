package snailmail.client

import snailmail.core.*
import snailmail.core.api.API
import snailmail.core.api.*
import snailmail.server.Server
import java.util.*

class Client(val server : Server) {
    private var token : AuthToken? = null

    private fun acquireToken() : AuthToken =
        token ?: throw NoAuthTokenException("Token doesn't exist")

    fun findUser(username : String) : User {
        val authToken = acquireToken()
        return server.searchByUsername(authToken, username) ?:
        throw UserNotFoundException("This user doesn't exist")
    }

    fun sendMessage(username: String, message: String) : TextMessage {
        val authToken = acquireToken()
        val user = findUser(username)
        return server.sendTextMessage(authToken, message,
                server.getPersonalChatWith(authToken, user.id).id)
    }

    fun sendMessageToGroupChat(chatTitle: String, message: String) : TextMessage {
        val authToken = acquireToken()
        val chat = findGroupChat(chatTitle)
        return server.sendTextMessage(authToken, message, chat.id)
    }

    fun findAvailableChats() : List<Chat> {
        val t = acquireToken()
        return server.getAvailableChats(t).getChats()
    }

    private fun findPersonalChatWith(username: String) : PersonalChat {
        val authToken = acquireToken()
        val user = findUser(username)
        return server.getPersonalChatWith(authToken, user.id)
    }

    fun getPersonalChatHistory(username: String) : List<Message> {
        val authToken = acquireToken()
        val chat = findPersonalChatWith(username)
        return server.getChatMessages(authToken, chat.id).getMessages()
    }

    private fun findGroupChat(chatTitle: String) : GroupChat {
        val authToken = acquireToken()
        return server.getAvailableChats(authToken).getChats().filterIsInstance<GroupChat>().find { it.title == chatTitle }
                ?: throw ChatNotFoundException("This group chat doesn't exist!")
    }

    fun getGroupChatHistory(chatTitle : String) : List<Message> {
        val authToken = acquireToken()
        val chat = findGroupChat(chatTitle)
        return server.getChatMessages(authToken, chat.id).getMessages()
    }

    fun createGroupChat(chatTitle: String, members: List<String>) : GroupChat {
        val authToken = acquireToken()
        val invitedMembers = members.map { findUser(it).id }
        return server.createGroupChat(authToken, chatTitle, invitedMembers)
    }
    fun authenticate(userCredentials: UserCredentials) : Boolean {
        val authenticationResult = server.authenticate(userCredentials)
        if (authenticationResult is AuthSuccessful)
            token = authenticationResult.token
        return authenticationResult.successful
    }

    fun register(userCredentials: UserCredentials) : Boolean {
        val authenticationResult = server.register(userCredentials)
        if (authenticationResult is AuthSuccessful)
            token = authenticationResult.token
        return authenticationResult.successful
    }
}


