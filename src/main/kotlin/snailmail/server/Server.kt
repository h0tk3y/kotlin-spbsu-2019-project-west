package snailmail.server


import snailmail.core.*
import snailmail.core.api.*
import snailmail.server.jwt.JwtConfig
import java.util.*
import kotlin.collections.HashMap
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.JWT
import snailmail.server.jwt.JwtConfig.verifier
import java.util.UUID


class Server : API {
    private var userCredentials = HashMap<String, String>()
    var chats = mutableListOf<Chat>()
    private var userByUsername = HashMap<String, User>()
    private var userById = HashMap<UUID, User>()
    var messagesByChatId = HashMap<UUID, MutableList<Message>>()
    private var chatByChatId = HashMap<UUID, Chat>()

    private fun tokenIsValid(token: AuthToken): UUID? {
        try {
            val decodedJWT = verifier.verify(token)
            val userId = UUID.fromString(decodedJWT.getClaim("userID").asString())
            if (userById.contains(userId)) {
                return userId
            }
        } catch (exception: JWTVerificationException) {
            return null
        }
        return null
    }

    override fun authenticate(credentials: UserCredentials): AuthenticationResult {
        val username = credentials.username
        val password = credentials.password
        if (userCredentials.contains(username) && userCredentials[username] == password) {
            val user = userByUsername[username]
                ?: throw InternalServerErrorException("Successful authentication, but user doesn't exist.")
            val token = JwtConfig.makeToken(user)
            tokenIsValid(token)
            return AuthSuccessful(token)
        }
        return AuthWrongCredentials()
    }

    override fun register(credentials: UserCredentials): AuthenticationResult {
        if (userCredentials.contains(credentials.username)) return AuthRegisterFailed("")
        val user = User(UUID.randomUUID(), credentials.username, credentials.username)
        userCredentials[credentials.username] = credentials.password
        userByUsername[credentials.username] = user
        userById[user.id] = user
        return authenticate(credentials)
    }

    override fun getAvailableChats(token: AuthToken): ChatRetriever {
        val userId = tokenIsValid(token) ?: throw InvalidTokenException()
        return object : ChatRetriever {
            override fun getChats(): List<Chat> {
                return this@Server.chats.filter {
                    it.hasMember(userId)
                }
            }
        }
    }

    override fun getPersonalChatWith(token: AuthToken, user: UUID): PersonalChat {
        val userId = tokenIsValid(token) ?: throw InvalidTokenException()
        val res = chats.find {
            when (it) {
                is PersonalChat -> it.hasMember(userId) && it.hasMember(user)
                else -> false
            }
        }
        if (res == null) {
            val chat = PersonalChat(UUID.randomUUID(), userId, user)
            chats.add(chat)
            messagesByChatId[chat.id] = mutableListOf()
            chatByChatId[chat.id] = chat
            return chat
        }
        return res as PersonalChat
    }

    override fun getChatMessages(token: AuthToken, chat: UUID): MessageRetriever {
        val userId = tokenIsValid(token) ?: throw InvalidTokenException()
        val currentChat = chatByChatId[chat]
        if (currentChat == null)
            throw InvalidChatId()
        else if (!currentChat.hasMember(userId))
            throw UserIsNotMemberException()
        return object : MessageRetriever {
            override fun getMessages(): List<Message> {
                return messagesByChatId[chat]
                    ?: throw InternalServerErrorException("Сhat exists, but his history doesn't exist.")
            }

            override fun getMessagesSince(since: Date): List<Message> {
                return getMessages().filter { it.date.after(since) }
            }
        }
    }

    override fun sendTextMessage(token: AuthToken, text: String, chat: UUID): TextMessage {
        val userId = tokenIsValid(token) ?: throw InvalidTokenException()
        val date: Date = Calendar.getInstance().run {
            time
        }
        val msg = TextMessage(
            id = UUID.randomUUID(), chatId = chat,
            sender = userId,
            content = text, date = date
        )
        messagesByChatId[chat]?.add(msg)
            ?: throw InternalServerErrorException("Сhat exists, but his history doesn't exist.")
        return msg
    }

    override fun createGroupChat(token: AuthToken, title: String, invitedMembers: List<UUID>): GroupChat {
        val userId = tokenIsValid(token) ?: throw InvalidTokenException()
        val chat = GroupChat(
            UUID.randomUUID(), title, userId,
            invitedMembers
        )
        chats.add(chat)
        chatByChatId[chat.id] = chat
        messagesByChatId[chat.id] = mutableListOf()
        return chat
    }

    override fun searchByUsername(token: AuthToken, username: String): User? {
        if (tokenIsValid(token) == null)
            throw InvalidTokenException()
        return userByUsername[username]
    }

    override fun getUserById(token: AuthToken, id: UUID): User? {
        if (tokenIsValid(token) == null)
            throw InvalidTokenException()
        return userById[id]
    }
}