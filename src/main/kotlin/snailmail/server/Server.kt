package snailmail.server


import snailmail.core.*
import snailmail.core.api.*
import java.util.*
import kotlin.collections.HashMap

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

    override fun authenticate(credentials: UserCredentials): AuthToken {
        val username = credentials.username
        val password = credentials.password
        if (!userCredentials.contains(username) || userCredentials[username] != password)
            throw WrongCredentialsException()
        else {
            val token = username
            userIdByToken[token] = userByUsername[username]?.id
                    ?: throw InternalServerErrorException("Successful authentication, but user doesn't exist.")
            return token
        }
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

    override fun getAvailableChats(token: AuthToken): List<Chat> {
        if (!tokenIsValid(token))
            throw InvalidTokenException()
        return this.chats.filter {
            it.hasMember(
                    userIdByToken[token]
                            ?: throw InternalServerErrorException("Token is valid, but user doesn't exist.")
            )
        }
    }

    override fun getPersonalChatWith(token: AuthToken, user: UUID): PersonalChat {
        if (!tokenIsValid(token))
            throw InvalidTokenException()
        val res = chats.find {
            when (it) {
                is PersonalChat -> it.hasMember(
                        userIdByToken[token]
                                ?: throw InternalServerErrorException("Token is valid, but user doesn't exist.")
                )
                        && it.hasMember(user)
                else -> false
            }
        }
        if (res == null) {
            val chat = PersonalChat(
                    UUID.randomUUID(), userIdByToken[token]
                    ?: throw InternalServerErrorException("Token is valid, but user doesn't exist."), user
            )
            chats.add(chat)
            messagesByChatId[chat.id] = mutableListOf()
            chatByChatId[chat.id] = chat
            return chat
        }
        return res as PersonalChat
    }

    override fun getChatMessages(token: AuthToken, chat: UUID): List<Message> {
        if (!tokenIsValid(token))
            throw InvalidTokenException()
        val currentChat = chatByChatId[chat]
        if (currentChat == null)
            throw InvalidChatIdException()
        else if (!currentChat.hasMember(
                        userIdByToken[token]
                                ?: throw InternalServerErrorException("Token is valid, but user doesn't exist.")
                )
        )
            throw UserIsNotMemberException()
        return messagesByChatId[chat]
                ?: throw InternalServerErrorException("Сhat exists, but his history doesn't exist.")
    }

    override fun sendTextMessage(token: AuthToken, text: String, chat: UUID): TextMessage {
        if (!tokenIsValid(token))
            throw InvalidTokenException()
        val date: Date = Calendar.getInstance().run {
            time
        }
        val msg = TextMessage(
                id = UUID.randomUUID(), chatId = chat,
                sender = userIdByToken[token]
                        ?: throw InternalServerErrorException("Token is valid, but user doesn't exist."),
                content = text, date = date
        )
        messagesByChatId[chat]?.add(msg)
                ?: throw InternalServerErrorException("Сhat exists, but his history doesn't exist.")
        return msg
    }

    override fun createGroupChat(token: AuthToken, title: String, invitedMembers: List<UUID>): GroupChat {
        if (!tokenIsValid(token))
            throw InvalidTokenException()
        val chat = GroupChat(
                UUID.randomUUID(), title, userIdByToken[token]
                ?: throw InternalServerErrorException("Token is valid, but user doesn't exist."),
                invitedMembers
        )
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