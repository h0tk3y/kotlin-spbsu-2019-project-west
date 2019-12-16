package snailmail.server


import snailmail.core.*
import snailmail.server.data.DataBase
import java.util.*

class Server(private val secretKey: String = "secret", private val dataBase: DataBase) : Api {

    //add checking if the change was successful or not
    override fun changeCredentials(authToken: AuthToken, credentials: UserCredentials): AuthToken {
        val userId = getUserIdFromToken(authToken)
        if ((dataBase.getUserById(userId)?.username ?: throw UserDoesNotExistException()) != credentials.username)
            throw ProtocolErrorException()
        dataBase.changePassword(credentials)
        return generateToken(userId)
    }

    //
    override fun getChatById(token: AuthToken, chat: UUID): Chat {
        val userId = getUserIdFromToken(token)
        val groupChat = dataBase.getChatByChatId(chat) ?: throw ChatDoesNotExistOrUnavailableException()
        if (!groupChat.hasMember(userId))
            throw UserIsNotMemberException()
        return dataBase.getChatByChatId(chat) ?: throw ChatDoesNotExistOrUnavailableException()
    }

    private fun joinGroupChatById(userId: UUID, chatId: UUID): GroupChat {
        val groupChat = dataBase.getChatByChatId(chatId) ?: throw ChatDoesNotExistOrUnavailableException()
        if (groupChat !is GroupChat)
            throw ChatDoesNotExistOrUnavailableException()
        if (groupChat.blacklist.contains(userId))
            throw ChatDoesNotExistOrUnavailableException()
        if (groupChat.members.contains(userId))
            throw UserIsAlreadyMemberException()
        dataBase.joinGroupChat(userId, groupChat.id)
        return groupChat
    }

    override fun joinGroupChatUsingPublicTag(token: AuthToken, tag: String): GroupChat {
        val userId = getUserIdFromToken(token)
        val groupChatId = dataBase.getGroupChatIdByTag(tag) ?: throw PublicTagIsUnavailableException()
        return joinGroupChatById(userId, groupChatId)
    }

    override fun joinGroupChatUsingInviteToken(token: AuthToken, inviteToken: String): GroupChat {
        val userId = getUserIdFromToken(token)
        val chatId = getChatIdFromToken(inviteToken)
        return joinGroupChatById(userId, chatId)
    }

    //add sending of service message
    override fun inviteUserToGroupChat(token: AuthToken, chat: UUID, user: UUID): GroupChat {
        val userId = getUserIdFromToken(token)
        if (!dataBase.findUserById(user))
            throw UserDoesNotExistException()
        val groupChat = dataBase.getChatByChatId(chat) ?: throw ChatDoesNotExistOrUnavailableException()
        if (groupChat !is GroupChat)
            throw ChatDoesNotExistOrUnavailableException()
        if (groupChat.members.contains(user))
            throw UserIsAlreadyMemberException()
        if (!groupChat.members.contains(userId))
            throw UserIsNotMemberException()
        if (checkBannedFor(token, user))
            throw UserIsBannedException()
        dataBase.joinGroupChat(user, chat)
        if (groupChat.blacklist.contains(user))
            dataBase.removeUserFromBlackListOfGroupChat(user, chat)
        return dataBase.getChatByChatId(chat) as GroupChat
    }

    override fun kickUserFromGroupChat(token: AuthToken, chat: UUID, user: UUID): GroupChat {
        val userId = getUserIdFromToken(token)
        if (userId == user)
            throw ProtocolErrorException("Impossible to kick yoursels, use leaveGroupChat() instead")
        if (!dataBase.findUserById(user))
            throw UserDoesNotExistException()
        if (!dataBase.findGroupChatById(chat))
            throw ChatDoesNotExistOrUnavailableException()
        if (!dataBase.isOwnerOfGroupChat(userId, chat))
            throw UserIsNotOwnerException()
        if (dataBase.isOwnerOfGroupChat(user, chat))
            throw OperationFailedException("Impossible to kick owner out of group chat")
        dataBase.removeUserFromGroupChat(user, chat)
        dataBase.addUserToBlacklistOfGroupChat(user, chat)
        return dataBase.getChatByChatId(chat) as GroupChat
    }

    override fun removeUserFromBlacklistOfGroupChat(token: AuthToken, chat: UUID, user: UUID): GroupChat {
        val userId = getUserIdFromToken(token)
        if (!dataBase.findUserById(user))
            throw UserDoesNotExistException()
        if (!dataBase.findGroupChatById(chat))
            throw ChatDoesNotExistOrUnavailableException()
        if (!dataBase.isOwnerOfGroupChat(userId, chat))
            throw UserIsNotOwnerException()
        dataBase.removeUserFromBlackListOfGroupChat(user, chat)
        return dataBase.getChatByChatId(chat) as GroupChat
    }

    //add service message
    override fun leaveGroupChat(token: AuthToken, chat: UUID): GroupChat {
        val userId = getUserIdFromToken(token)
        val groupChat = dataBase.getChatByChatId(chat) ?: throw ChatDoesNotExistOrUnavailableException()
        if (groupChat !is GroupChat)
            throw ChatDoesNotExistOrUnavailableException()
        if (!dataBase.isMemberOfGroupChat(userId, groupChat.id))
            throw UserIsNotMemberException()
        if (dataBase.isOwnerOfGroupChat(userId, groupChat.id)) {
            dataBase.removeUserFromGroupChat(userId, groupChat.id)
            val members = dataBase.getMembersOfChat(chat)
            if (members == null || members.isEmpty()) {
                dataBase.deleteChat(groupChat.id)
                return groupChat
            }
            val newOwner = members[0]
            dataBase.setOwnerOfGroupChat(newOwner, groupChat.id)
            return GroupChat(groupChat.id, groupChat.title, newOwner, members,
                groupChat.avatar, groupChat.blacklist, groupChat.publicTag, groupChat.privateInviteToken)
        } else {
            dataBase.removeUserFromGroupChat(userId, groupChat.id)
        }
        return groupChat
    }

    //add service message
    override fun changeTitleOfGroupChat(token: AuthToken, chat: UUID, title: String): GroupChat {
        val userId = getUserIdFromToken(token)
        if (!dataBase.findGroupChatById(chat))
            throw ChatDoesNotExistOrUnavailableException()
        if (!dataBase.isOwnerOfGroupChat(userId, chat))
            throw UserIsNotOwnerException()
        dataBase.setTitleOfGroupChat(title, chat)
        return dataBase.getChatByChatId(chat) as GroupChat
    }

    //add service message
    override fun updateAvatarOfGroupChat(token: AuthToken, chat: UUID, photo: Photo): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updatePublicTagOfGroupChat(token: AuthToken, chat: UUID, publicTag: String): GroupChat {
        val userId = getUserIdFromToken(token)
        if (!dataBase.findGroupChatById(chat))
            throw ChatDoesNotExistOrUnavailableException()
        if (!dataBase.isOwnerOfGroupChat(userId, chat))
            throw UserIsNotOwnerException()
        dataBase.setPublicTagOfGroupChat(publicTag, chat)
        return dataBase.getChatByChatId(chat) as GroupChat
    }

    override fun withdrawPublicTagOfGroupChat(token: AuthToken, chat: UUID): GroupChat {
        val userId = getUserIdFromToken(token)
        val groupChat = dataBase.getChatByChatId(chat) ?: throw ChatDoesNotExistOrUnavailableException()
        if (groupChat !is GroupChat)
            throw ChatDoesNotExistOrUnavailableException()
        if (groupChat.publicTag == null)
            throw ProtocolErrorException("Public tag doesn't exist")
        if (!dataBase.isOwnerOfGroupChat(userId, chat))
            throw UserIsNotOwnerException()
        dataBase.withdrawPublicTagOfGroupChat(chat)
        return dataBase.getChatByChatId(chat) as GroupChat
    }

    override fun makeNewPrivateInviteToken(token: AuthToken, chat: UUID): GroupChat {
        val userId = getUserIdFromToken(token)
        if (!dataBase.findGroupChatById(chat))
            throw ChatDoesNotExistOrUnavailableException()
        if (!dataBase.isOwnerOfGroupChat(userId, chat))
            throw UserIsNotOwnerException()
        val newInviteToken = generateInviteToken(chat)
        dataBase.setPrivateInviteTokenOfGroupChat(newInviteToken, chat)
        return dataBase.getChatByChatId(chat) as GroupChat
    }

    override fun withdrawPrivateInviteToken(token: AuthToken, chat: UUID): GroupChat {
        val userId = getUserIdFromToken(token)
        val groupChat = dataBase.getChatByChatId(chat) ?: throw ChatDoesNotExistOrUnavailableException()
        if (groupChat !is GroupChat)
            throw ChatDoesNotExistOrUnavailableException()
        if (groupChat.privateInviteToken == null)
            throw ProtocolErrorException("Invite token doesn't exist")
        if (!dataBase.isOwnerOfGroupChat(userId, chat))
            throw UserIsNotOwnerException()
        dataBase.withdrawPrivateInviteTokenOfGroupChat(chat)
        return dataBase.getChatByChatId(chat) as GroupChat
    }

    //add GroupChatPreferences table to DB
    override fun setPreferredTitleOfGroupChat(token: AuthToken, chat: UUID, title: String): GroupChatPreferences {
        val userId = getUserIdFromToken(token)
        if (!dataBase.findGroupChatById(chat))
            throw ChatDoesNotExistOrUnavailableException()
        if (!dataBase.isMemberOfGroupChat(userId, chat))
            throw UserIsNotMemberException()
        dataBase.setPreferredTiTleOfGroupChat(userId, chat, title)
        return dataBase.getGroupChatPreferencesByChatId(userId, chat) ?: throw GroupChatPreferencesDoesNotExist()
    }

    override fun getGroupChatPreferences(token: AuthToken, chat: UUID): GroupChatPreferences {
        val userId = getUserIdFromToken(token)
        if (!dataBase.findGroupChatById(chat))
            throw ChatDoesNotExistOrUnavailableException()
        if (!dataBase.isOwnerOfGroupChat(userId, chat))
            throw UserIsNotMemberException()
        return dataBase.getGroupChatPreferencesByChatId(userId, chat) ?: throw GroupChatPreferencesDoesNotExist()
    }

    override fun prepareUpload(token: AuthToken, mediaType: String): UUID {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMedia(token: AuthToken, media: UUID): Media {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteMessage(token: AuthToken, messageId: UUID): DeletedMessage {
        val userId = getUserIdFromToken(token)
        val message = dataBase.getMessageById(messageId) ?: throw MessageDoesNotExistException()
        if (message.sender != userId)
            throw UserIsNotSenderException()
        dataBase.addToDeletedMessages(message.id)
        dataBase.deleteMessage(message.id)
        return message as DeletedMessage
    }

    override fun editTextMessage(token: AuthToken, messageId: UUID, newText: String): TextMessage {
        val userId = getUserIdFromToken(token)
        val message = dataBase.getMessageById(messageId) ?: throw MessageDoesNotExistException()
        if (message.sender != userId)
            throw UserIsNotSenderException()
        dataBase.editTextMessage(messageId, newText)
        return dataBase.getTextMessage(messageId) ?: throw MessageDoesNotExistException()
    }

    override fun sendMediaMessage(token: AuthToken, media: UUID, caption: String): MediaMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun editMediaMessageCaption(token: AuthToken, message: UUID, newCaption: String): MediaMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContact(token: AuthToken, user: UUID): Contact {
        val userId = getUserIdFromToken(token)
        if (!dataBase.findUserById(userId))
            throw UserDoesNotExistException()
        return dataBase.getContactOfUser(userId, user) ?: throw ContactDoesNotExist()
    }

    override fun updateContactDisplayName(token: AuthToken, user: UUID, displayName: String): Contact {
        val userId = getUserIdFromToken(token)
        if (!dataBase.findUserById(userId))
            throw UserDoesNotExistException()
        val contact = dataBase.getContactOfUser(userId, user) ?: throw ContactDoesNotExist()
        dataBase.changeContactDisplayName(userId, user, displayName)
        return contact
    }

    override fun banContact(token: AuthToken, user: UUID): Contact {
        val userId = getUserIdFromToken(token)
        if (!dataBase.findUserById(userId))
            throw UserDoesNotExistException()
        val contact = dataBase.getContactOfUser(userId, user) ?: throw ContactDoesNotExist()
        dataBase.changeBannedContactOfUser(userId, user, true)
        return contact
    }

    override fun unbanContact(token: AuthToken, user: UUID): Contact {
        val userId = getUserIdFromToken(token)
        if (!dataBase.findUserById(userId))
            throw UserDoesNotExistException()
        val contact = dataBase.getContactOfUser(userId, user) ?: throw ContactDoesNotExist()
        dataBase.changeBannedContactOfUser(userId, user, false)
        return contact
    }

    override fun checkBannedFor(token: AuthToken, user: UUID): Boolean {
        val userId = getUserIdFromToken(token)
        if (!dataBase.findUserById(userId))
            throw UserDoesNotExistException()
        val contact = dataBase.getContactOfUser(userId, user) ?: throw ContactDoesNotExist()
        return contact.banned
    }

    override fun updateProfileDisplayName(token: AuthToken, displayName: String): User {
        val userId = getUserIdFromToken(token)
        val user = dataBase.getUserById(userId) ?: throw UserDoesNotExistException()
        dataBase.updateProfileDisplayName(userId, displayName)
        return user
    }

    override fun updateProfileEmail(token: AuthToken, email: String?): User {
        val userId = getUserIdFromToken(token)
        val user = dataBase.getUserById(userId) ?: throw UserDoesNotExistException()
        dataBase.updateProfileEmail(userId, email)
        return user
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
            val jwt = simpleJwt.verifier.verify(token)
            id = UUID.fromString(jwt.getClaim("id").asString())
        } catch (e: Exception) {
            throw InvalidTokenException()
        }
        if (!dataBase.findUserById(id))
            throw InvalidTokenException()
        return id
    }

    private fun getChatIdFromToken(token: String): UUID {
        return getUserIdFromToken(token)
    }

    private fun generateToken(userId: UUID): AuthToken = simpleJwt.sign(userId)
    private fun generateInviteToken(groupChatId: UUID): String = simpleJwt.sign(groupChatId).toString()

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
        if (!dataBase.findUserById(userId))
            throw UserDoesNotExistException()
        return dataBase.getChats(userId) ?: listOf()
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