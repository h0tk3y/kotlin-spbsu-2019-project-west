package snailmail.client

import snailmail.core.*
import snailmail.core.api.API
import snailmail.core.api.*
import snailmail.server.Server

class Client(val server : Server) {
    private var token : AuthToken? = null

    private fun checkToken() : AuthToken {
        val t = token
        if (t == null)
            throw NullTokenException("Token doesn't exist")
        else
            return t
    }

    fun findUser(username : String) : User {
        val t = checkToken()
        return server.searchByUsername(t, username) ?:
        throw UserNotFoundException("This user doesn't exist")
    }

    fun sendMessage(username: String, message: String) : TextMessage {
        val user = findUser(username)
        val t = checkToken()
        return server.sendTextMessage(t, message,
                server.getPersonalChatWith(t, user.id).id)
    }

    fun findAvailableChats() : List<Chat> {
        val t = checkToken()
        return server.getAvailableChats(t).getChats()
    }

    private fun findPersonalChat(username: String) : Chat {
        val user = findUser(username)
        val t = checkToken()
        return server.getPersonalChatWith(t, user.id)
    }

    fun getPersonalChatHistory(username: String) : List<Message> {
        val chat = findPersonalChat(username)
        val t = checkToken()
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


