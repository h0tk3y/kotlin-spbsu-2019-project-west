package snailmail.server


import snailmail.core.*
import snailmail.server.data.MySQL
import java.util.*

class Server(private val secretKey: String = "secret", private val dataBase: MySQL) : Api {
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

    private fun getUserIdFromToken(token: AuthToken): UUID {
        val id: UUID
        try {
            id = UUID.fromString(simpleJwt.verifier.verify(token).getClaim("id").asString())
        } catch (e: Exception) {
            throw InvalidTokenException()
        }
        if (!dataBase.findUserById(id))
            throw InvalidTokenException()
        return id
    }

    private fun generateToken(userId: UUID): AuthToken = simpleJwt.sign(userId)

    override fun authenticate(credentials: UserCredentials): AuthToken {
        val username = credentials.username
        val password = credentials.password

        if (username.isEmpty() || password.isEmpty())
            throw ProtocolErrorException()

        if (!dataBase.verifyUserCredentials(username, password))
            throw WrongCredentialsException()

        val userId = dataBase.getUserByUsername(username)?.id
                ?: throw InternalServerErrorException("Successful authentication, but user doesn't exist.")
        return generateToken(userId)
    }

    override fun register(credentials: UserCredentials): AuthToken {
        val username = credentials.username
        val password = credentials.password
        if (username.isEmpty() || password.isEmpty())
            throw ProtocolErrorException()
        if (dataBase.findUsername(username))
            throw UnavailableUsernameException()
        val user = User(UUID.randomUUID(), username, username)
        dataBase.addUserCredentials(username, password)
        dataBase.addUser(user)
        return authenticate(credentials)
    }

    override fun getChats(token: AuthToken): List<Chat> {
        val userId = getUserIdFromToken(token)
        return dataBase.getChats(userId)
    }

    override fun getPersonalChatWith(token: AuthToken, user: UUID): PersonalChat {
        val userId = getUserIdFromToken(token)
        val res = dataBase.getPersonalChatWith(userId, user)
        if (res == null) {
            val chat = PersonalChat(
                    UUID.randomUUID(), userId, user
            )
            dataBase.addChat(chat)
            return chat
        }
        return res as PersonalChat
    }

    override fun getChatMessages(token: AuthToken, chat: UUID): List<Message> {
        val userId = getUserIdFromToken(token)
        val currentChat = dataBase.getChatByChatId(chat) ?: throw ChatDoesNotExistOrUnavailableException()
        if (!currentChat.hasMember(userId))
            throw UserIsNotMemberException()
        return dataBase.getMessagesByChatId(chat)
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
        if (!dataBase.findMessagesByChatId(chat)) {
            throw InternalServerErrorException("Сhat exists, but his history doesn't exist.")
        } else {
            dataBase.addMessage(chat, msg)
        }
        return msg
    }

    override fun createGroupChat(token: AuthToken, title: String, invitedMembers: List<UUID>): GroupChat {
        val userId = getUserIdFromToken(token)
        val chat = GroupChat(
                UUID.randomUUID(), title, userId,
                invitedMembers
        )
        dataBase.addChat(chat)
        return chat
    }

    override fun getUserByUsername(token: AuthToken, username: String): User {
        getUserIdFromToken(token)
        return dataBase.getUserByUsername(username) ?: throw UserDoesNotExistException()
    }

    override fun getUserById(token: AuthToken, user: UUID): User {
        getUserIdFromToken(token)
        return dataBase.getUserById(user) ?: throw UserDoesNotExistException()
    }
}