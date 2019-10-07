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
        val validToken = acquireToken()
        return server.searchByUsername(validToken, username) ?:
        throw UserNotFoundException("This user doesn't exist")
    }

    fun sendMessage(username: String, message: String) : TextMessage {
        val user = findUser(username)
        val validToken = acquireToken()
        return server.sendTextMessage(validToken, message,
                server.getPersonalChatWith(validToken, user.id).id)
    }

    fun findAvailableChats() : List<Chat> {
        val validToken = acquireToken()
        return server.getAvailableChats(validToken).getChats()
    }

    private fun findPersonalChat(username: String) : Chat {
        val user = findUser(username)
        val validToken = acquireToken()
        return server.getPersonalChatWith(validToken, user.id)
    }

    fun getPersonalChatHistory(username: String) : List<Message> {
        val chat = findPersonalChat(username)
        val validToken = acquireToken()
        return server.getChatMessages(validToken, chat.id).getMessages()
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


