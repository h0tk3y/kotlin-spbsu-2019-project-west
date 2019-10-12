package snailmail.client

import snailmail.core.*
import snailmail.core.api.*
import snailmail.server.Server

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
        val user = findUser(username)
        val authToken = acquireToken()
        return api.sendTextMessage(authToken, message,
                api.getPersonalChatWith(authToken, user.id).id)
    }

    fun findAvailableChats() : List<Chat> {
        val authToken = acquireToken()
        return api.getAvailableChats(authToken).getChats()
    }

    private fun findPersonalChat(username: String) : Chat {
        val user = findUser(username)
        val authToken = acquireToken()
        return api.getPersonalChatWith(authToken, user.id)
    }

    fun getPersonalChatHistory(username: String) : List<Message> {
        val chat = findPersonalChat(username)
        val authToken = acquireToken()
        return api.getChatMessages(authToken, chat.id).getMessages()
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


