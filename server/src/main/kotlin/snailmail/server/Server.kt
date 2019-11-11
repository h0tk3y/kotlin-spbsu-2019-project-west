package snailmail.server


import com.auth0.jwt.exceptions.JWTVerificationException
import snailmail.core.*
import java.util.*
import kotlin.collections.HashMap

class Server(private val secretKey: String = "secret") : Api {
    private val simpleJwt = SimpleJwt(secretKey)
    private var userCredentials = HashMap<String, String>()
    private var chats = mutableListOf<Chat>()
    private var userByUsername = HashMap<String, User>()
    private var userById = HashMap<UUID, User>()
    private var messagesByChatId = HashMap<UUID, MutableList<Message>>()
    private var chatByChatId = HashMap<UUID, Chat>()

    private fun getUserIdFromToken(token: AuthToken): UUID {
        try {
            return UUID.fromString(simpleJwt.verifier.verify(token).getClaim("id").toString())
        } catch (e: JWTVerificationException) {
            throw InvalidTokenException()
        }
    }

    private fun generateToken(userId: UUID): AuthToken = simpleJwt.sign(userId)

    override fun authenticate(credentials: UserCredentials): AuthToken {
        val username = credentials.username
        val password = credentials.password
        if (!userCredentials.contains(username) || userCredentials[username] != password)
            throw WrongCredentialsException()

        val userId = userByUsername[username]?.id
                ?: throw InternalServerErrorException("Successful authentication, but user doesn't exist.")
        return generateToken(userId)
    }

    override fun register(credentials: UserCredentials): AuthToken {
        if (userCredentials.contains(credentials.username))
            throw UnavailableUsernameException()
        val user = User(UUID.randomUUID(), credentials.username, credentials.username)
        userCredentials[credentials.username] = credentials.password
        userByUsername[credentials.username] = user
        userById[user.id] = user
        return authenticate(credentials)
    }

    override fun getChats(token: AuthToken): List<Chat> {
        val userId = getUserIdFromToken(token)
        return this.chats.filter {
            it.hasMember(userId)
        }
    }

    override fun getPersonalChatWith(token: AuthToken, user: UUID): PersonalChat {
        val userId = getUserIdFromToken(token)
        val res = chats.find {
            when (it) {
                is PersonalChat -> it.hasMember(getUserIdFromToken(token))
                        && it.hasMember(user)
                else -> false
            }
        }
        if (res == null) {
            val chat = PersonalChat(
                    UUID.randomUUID(), userId, user
            )
            chats.add(chat)
            messagesByChatId[chat.id] = mutableListOf()
            chatByChatId[chat.id] = chat
            return chat
        }
        return res as PersonalChat
    }

    override fun getChatMessages(token: AuthToken, chat: UUID): List<Message> {
        val userId = getUserIdFromToken(token)
        val currentChat = chatByChatId[chat] ?: throw InvalidChatIdException()
        if (!currentChat.hasMember(userId))
            throw UserIsNotMemberException()
        return messagesByChatId[chat]
                ?: throw InternalServerErrorException("Сhat exists, but his history doesn't exist.")
    }

    override fun sendTextMessage(token: AuthToken, text: String, chat: UUID): TextMessage {
        val userId = getUserIdFromToken(token)
        val date: Date = Calendar.getInstance().run {
            time
        }
        val msg = TextMessage(
                id = UUID.randomUUID(), chat = chat,
                sender = userId,
                content = text, date = date
        )
        messagesByChatId[chat]?.add(msg)
                ?: throw InternalServerErrorException("Сhat exists, but his history doesn't exist.")
        return msg
    }

    override fun createGroupChat(token: AuthToken, title: String, invitedMembers: List<UUID>): GroupChat {
        val userId = getUserIdFromToken(token)
        val chat = GroupChat(
                UUID.randomUUID(), title, userId,
                invitedMembers
        )
        chats.add(chat)
        chatByChatId[chat.id] = chat
        messagesByChatId[chat.id] = mutableListOf()
        return chat
    }

    override fun getUserByUsername(token: AuthToken, username: String): User? {
        val userId = getUserIdFromToken(token)
        return userByUsername[username]
    }

    override fun getUserById(token: AuthToken, id: UUID): User? {
        val userId = getUserIdFromToken(token)
        return userById[id]
    }
}