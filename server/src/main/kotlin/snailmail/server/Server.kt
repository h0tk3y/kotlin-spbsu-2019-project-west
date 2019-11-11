package snailmail.server


import com.auth0.jwt.exceptions.JWTVerificationException
import snailmail.core.*
import java.util.*
import kotlin.collections.HashMap

class Server(private val secretKey: String = "secret") : Api {
    override fun changeCredentials(authToken: AuthToken, credentials: UserCredentials): AuthToken {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getChatById(token: AuthToken, chat: UUID): Chat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun joinGroupChatUsingPublicTag(token: AuthToken, tag: String): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun joinGroupChatUsingInviteToken(token: AuthToken, inviteToken: String): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun inviteUserToGroupChat(token: AuthToken, chat: UUID, user: UUID): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun kickUserFromGroupChat(token: AuthToken, chat: UUID, user: UUID): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeUserFromBlacklistOfGroupChat(token: AuthToken, chat: UUID, user: UUID): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun leaveGroupChat(token: AuthToken, chat: UUID): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun changeTitleOfGroupChat(token: AuthToken, chat: UUID, title: String): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateAvatarOfGroupChat(token: AuthToken, chat: UUID, photo: Photo): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updatePublicTagOfGroupChat(token: AuthToken, chat: UUID, publicTag: String): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun withdrawPublicTagOfGroupChat(token: AuthToken, chat: UUID): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun makeNewPrivateInviteToken(token: AuthToken, chat: UUID): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun withdrawPrivateInviteToken(token: AuthToken, chat: UUID): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setPreferredTitleOfGroupChat(token: AuthToken, chat: UUID, title: String): GroupChatPreferences {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGroupChatPreferences(token: AuthToken, chat: UUID): GroupChatPreferences {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun prepareUpload(token: AuthToken, mediaType: String): UUID {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMedia(token: AuthToken, media: UUID): Media {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteMessage(token: AuthToken, message: UUID): DeletedMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun editTextMessage(token: AuthToken, message: UUID, newText: String): TextMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendMediaMessage(token: AuthToken, media: UUID, caption: String): MediaMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun editMediaMessageCaption(token: AuthToken, message: UUID, newCaption: String): MediaMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContact(token: AuthToken, user: UUID): Contact {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateContactDisplayName(token: AuthToken, user: UUID, displayName: String): Contact {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun banContact(token: AuthToken, user: UUID): Contact {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unbanContact(token: AuthToken, user: UUID): Contact {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun checkBannedFor(token: AuthToken, user: UUID): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateProfileDisplayName(token: AuthToken, displayName: String): User {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateProfileEmail(token: AuthToken, email: String?): User {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateProfileAvatar(token: AuthToken, avatar: Photo?): User {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun searchAmongMessages(token: AuthToken, text: String): List<Message> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun searchAmongUsers(token: AuthToken, text: String): List<User> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun searchAmongChats(token: AuthToken, text: String): List<Chat> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

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
        val currentChat = chatByChatId[chat] ?: throw ChatDoesNotExistOrUnavailableException()
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

    override fun getUserByUsername(token: AuthToken, username: String): User {
        getUserIdFromToken(token)
        return userByUsername[username] ?: throw UserDoesNotExistException()
    }

    override fun getUserById(token: AuthToken, user: UUID): User {
        getUserIdFromToken(token)
        return userById[user] ?: throw UserDoesNotExistException()
    }
}