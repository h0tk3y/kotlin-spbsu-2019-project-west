package snailmail.server

import snailmail.core.*
import snailmail.core.api.*
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

class Server : API {
    var userCredentials = HashMap<String, String>()
    var chats = mutableListOf<Chat>()
    var userByUsername = HashMap<String, User>()
    var idByToken = HashMap<String, UUID>()
    var messagesByChatId = HashMap<UUID, MutableList<Message>>()
    var chatByChatId = HashMap<UUID, Chat>()

    private fun tokenIsValid(token: AuthToken) : Boolean {
        return (idByToken[token] != null)
    }

    override fun authenticate(credentials: UserCredentials): AuthenticationResult {
        return if (userCredentials.contains(credentials.username) &&
                userCredentials[credentials.username] == credentials.password) {
            val token = credentials.username
            idByToken[token] = userByUsername[credentials.username]!!.id
            AuthSuccessful(token)
        }
        else AuthWrongCredentials()
    }

    override fun register(credentials: UserCredentials): AuthenticationResult {
        return if (userCredentials.contains(credentials.username)) AuthRegisterFailed("")
        else {
            userCredentials[credentials.username] = credentials.password
            userByUsername[credentials.username] = User(UUID.randomUUID(), credentials.username, credentials.username)
            authenticate(credentials)
        }
    }

    override fun getAvailableChats(token: AuthToken): ChatRetriever {
        if (!tokenIsValid(token))
            throw Exception("((")
        return object : ChatRetriever {
            override fun getChats() : List<Chat> {
                return this@Server.chats.filter { it.hasMember(idByToken[token]!!) }
            }
        }
    }

    override fun getPersonalChatWith(token: AuthToken, user: UUID): PersonalChat {
        if (!tokenIsValid(token))
            throw Exception("((")
        val res = chats.find { when (it) {
            is PersonalChat -> it.hasMember(idByToken[token]!!) && it.hasMember(user)
            else -> false
        } }
        return if (res == null) {
            val chat = PersonalChat(UUID.randomUUID(), idByToken[token]!!, user)
            chats.add(chat)
            messagesByChatId[chat.id] = mutableListOf()
            chatByChatId[chat.id] = chat
            chat
        } else res as PersonalChat
    }

    override fun getChatMessages(token: AuthToken, chat: UUID): MessageRetriever {
        if (chatByChatId[chat] == null || !tokenIsValid(token) || !chatByChatId[chat]!!.hasMember(idByToken[token]!!))
            throw Exception("((")
        return object : MessageRetriever {
            override fun getMessages(): List<Message> {
                return messagesByChatId[chat]!!
            }

            override fun getMessagesSince(since: Date): List<Message> {
                return getMessages().filter { it.date.after(since) }
            }
        }
    }

    override fun sendTextMessage(token: AuthToken, text: String, chat: UUID): TextMessage {
        if (!tokenIsValid(token))
            throw Exception("((")
        val date : Date = Calendar.getInstance().run {
            time
        }
        val msg = TextMessage(id = UUID.randomUUID(), chatId = chat,
                sender = idByToken[token]!!, content = text, date = date)
        messagesByChatId[chat]!!.add(msg)
        return msg
    }

    override fun createGroupChat(token: AuthToken, title: String, invitedMembers: List<UUID>): GroupChat {
        if (!tokenIsValid(token))
            throw Exception("((")
        val chat = GroupChat(UUID.randomUUID(), idByToken[token]!!, invitedMembers)
        chats.add(chat)
        chatByChatId[chat.id] = chat
        messagesByChatId[chat.id] = mutableListOf()
        return chat
    }

    override fun searchByUsername(token: AuthToken, username: String): User? {
        if (!tokenIsValid(token))
            throw Exception("((")
        return userByUsername[username]
    }
}