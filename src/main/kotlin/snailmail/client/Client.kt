package snailmail.client

import snailmail.core.*
import snailmail.core.api.API
import snailmail.core.api.*
import snailmail.server.Server

class Client(val server : Server) {
    private var token : AuthToken? = null

    private fun acquireToken() : AuthToken =
        token ?: throw NoAuthTokenException("Token doesn't exist")

    fun findUser(username : String) : User {
        val t = acquireToken()
        return server.searchByUsername(t, username) ?:
        throw UserNotFoundException("This user doesn't exist")
    }

    fun sendMessage(username: String, message: String) : TextMessage {
        val user = findUser(username)
        val t = acquireToken()
        return server.sendTextMessage(t, message,
                server.getPersonalChatWith(t, user.id).id)
    }

    fun findAvailableChats() : List<Chat> {
        val t = acquireToken()
        return server.getAvailableChats(t).getChats()
    }

    private fun findPersonalChat(username: String) : Chat {
        val user = findUser(username)
        val t = acquireToken()
        return server.getPersonalChatWith(t, user.id)
    }

    fun getPersonalChatHistory(username: String) : List<Message> {
        val chat = findPersonalChat(username)
        val t = acquireToken()
        return server.getChatMessages(t, chat.id).getMessages()
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


