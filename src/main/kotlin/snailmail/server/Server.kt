package snailmail.server

import snailmail.core.*
import snailmail.core.api.*
import java.util.*
import kotlin.collections.HashMap

class InvalidTokenException : Exception("Invalid Token")
class InvalidChatId : Exception("Invalid chat id")
class UserIsNotMemberException : Exception("User is not a member of this chat")

class Server : API {
    private var userCredentials = HashMap<String, String>()
    var chats = mutableListOf<Chat>()
    private var userByUsername = HashMap<String, User>()
    private var userById = HashMap<UUID, User>()
    var userIdByToken = HashMap<String, UUID>()
    var messagesByChatId = HashMap<UUID, MutableList<Message>>()
    private var chatByChatId = HashMap<UUID, Chat>()

    private fun tokenIsValid(token: AuthToken): Boolean {
        return (userIdByToken[token] != null)
    }

    override fun authenticate(credentials: UserCredentials): AuthenticationResult {
        return if (userCredentials.contains(credentials.username) &&
                userCredentials[credentials.username] == credentials.password) {
            val token = credentials.username
            userIdByToken[token] = userByUsername[credentials.username]!!.id
            AuthSuccessful(token)
        } else AuthWrongCredentials()
    }

    override fun register(credentials: UserCredentials): AuthenticationResult {
        return if (userCredentials.contains(credentials.username)) AuthRegisterFailed("")
        else {
            val user = User(UUID.randomUUID(), credentials.username, credentials.username)
            userCredentials[credentials.username] = credentials.password
            userByUsername[credentials.username] = user
            userById[user.id] = user
            authenticate(credentials)
        }
    }

    override fun getAvailableChats(token: AuthToken): ChatRetriever {
        if (!tokenIsValid(token))
            throw InvalidTokenException()
        return object : ChatRetriever {
            override fun getChats(): List<Chat> {
                return this@Server.chats.filter { it.hasMember(userIdByToken[token]!!) }
            }
        }
    }

    override fun getPersonalChatWith(token: AuthToken, user: UUID): PersonalChat {
        if (!tokenIsValid(token))
            throw InvalidTokenException()
        val res = chats.find {
            when (it) {
                is PersonalChat -> it.hasMember(userIdByToken[token]!!) && it.hasMember(user)
                else -> false
            }
        }
        return if (res == null) {
            val chat = PersonalChat(UUID.randomUUID(), userIdByToken[token]!!, user)
            chats.add(chat)
            messagesByChatId[chat.id] = mutableListOf()
            chatByChatId[chat.id] = chat
            chat
        } else res as PersonalChat
    }

    override fun getChatMessages(token: AuthToken, chat: UUID): MessageRetriever {
        if (!tokenIsValid(token))
            throw InvalidTokenException()
        if (chatByChatId[chat] == null)
            throw InvalidChatId()
        if (!chatByChatId[chat]!!.hasMember(userIdByToken[token]!!))
            throw UserIsNotMemberException()
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
            throw InvalidTokenException()
        val date: Date = Calendar.getInstance().run {
            time
        }
        val msg = TextMessage(id = UUID.randomUUID(), chatId = chat,
                sender = userIdByToken[token]!!, content = text, date = date)
        messagesByChatId[chat]!!.add(msg)
        return msg
    }

    override fun createGroupChat(token: AuthToken, title: String, invitedMembers: List<UUID>): GroupChat {
        if (!tokenIsValid(token))
            throw InvalidTokenException()
        val chat = GroupChat(UUID.randomUUID(), title, userIdByToken[token]!!, invitedMembers)
        chats.add(chat)
        chatByChatId[chat.id] = chat
        messagesByChatId[chat.id] = mutableListOf()
        return chat
    }

    override fun searchByUsername(token: AuthToken, username: String): User? {
        if (!tokenIsValid(token))
            throw InvalidTokenException()
        return userByUsername[username]
    }

    override fun getUserById(token: AuthToken, id: UUID): User? {
        if (!tokenIsValid(token))
            throw InvalidTokenException()
        return userById[id]
    }
}