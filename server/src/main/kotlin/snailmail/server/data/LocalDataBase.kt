package snailmail.server.data

import snailmail.core.*
import snailmail.server.SimpleJwt
import java.util.*
import kotlin.collections.HashMap

class LocalDataBase : DataBase {
    private var userCredentials = HashMap<String, String>()
    private var chats = mutableListOf<Chat>()
    private var userByUsername = HashMap<String, User>()
    private var userById = HashMap<UUID, User>()
    private var messagesByChatId = HashMap<UUID, MutableList<UUID>>()
    private var chatByChatId = HashMap<UUID, Chat>()
    private var contactsOfUser = HashMap<UUID, HashMap<UUID, Contact>>()
    private var messages = HashMap<UUID, Message>()
    private var deletedMessages = HashMap<UUID, DeletedMessage>()
    private var groupChatPreferences = HashMap<UUID, HashMap<UUID, GroupChatPreferences>>()

    override fun verifyUserCredentials(username: String, password: String): Boolean {
        return (userCredentials.contains(username) && userCredentials[username] == password)
    }

    override fun getUserByUsername(username: String): User? {
        return userByUsername[username]
    }

    override fun getUserById(id: UUID): User? {
        return userById[id]
    }

    override fun getChats(userId: UUID): List<Chat> {
        return this.chats.filter {
            it.hasMember(userId)
        }
    }

    override fun getChatByChatId(id: UUID): Chat? {
        return chatByChatId[id]
    }

    override fun getPersonalChatWith(thisUser: UUID, otherUser: UUID): PersonalChat? {
        return chats.find {
            when (it) {
                is PersonalChat -> it.hasMember(thisUser)
                        && it.hasMember(otherUser)
                else -> false
            }
        } as PersonalChat
    }

    override fun getMessagesByChatId(id: UUID): List<Message>? {
        return messagesByChatId[id]?.mapNotNull { it -> messages[it] }
    }

    override fun addUserCredentials(username: String, password: String) {
        userCredentials[username] = password
    }

    override fun addUser(user: User) {
        userById[user.id] = user
        userByUsername[user.username] = user
    }

    override fun addChat(chat: Chat) {
        chats.add(chat)
        messagesByChatId[chat.id] = mutableListOf()
        chatByChatId[chat.id] = chat
    }

    override fun addMessage(chat: UUID, message: Message) {
        messages[message.id] = message
        messagesByChatId[chat]!!.add(message.id)
    }

    override fun deleteChat(id: UUID) {
        val chat = chatByChatId[id]?.let {
            chats.remove(it)
            messagesByChatId.remove(it.id)
        }
        chatByChatId.remove(id)
    }

    override fun findUserById(id: UUID): Boolean {
        return userById.containsKey(id)
    }

    override fun findUsername(username: String): Boolean {
        return userCredentials.containsKey(username)
    }

    override fun findMessagesByChatId(chat: UUID): Boolean {
        return messagesByChatId.containsKey(chat)
    }

    override fun changeBannedContactOfUser(userId: UUID, targetUserId: UUID, isBanned: Boolean) {
        contactsOfUser[userId]?.get(targetUserId)?.banned = isBanned
    }

    override fun changeContactDisplayName(userId: UUID, targetUserId: UUID, newDisplayName: String) {
        contactsOfUser[userId]?.get(targetUserId)?.displayName = newDisplayName
    }

    override fun changePassword(credentials: UserCredentials): Boolean {
        if (userCredentials.containsKey(credentials.username)) {
            userCredentials[credentials.username] = credentials.password
            return true
        }
        return false
    }

    override fun deleteMessage(messageId: UUID) {
        messages.remove(messageId)
    }

    override fun editTextMessage(messageId: UUID, text: String) {
        if (messages.containsKey(messageId) && messages[messageId] is TextMessage) {
            val message = messages[messageId] as TextMessage
            messages[messageId] = TextMessage(
                message.id, message.chat, message.date,
                message.sender, text, message.edited
            )
        }
    }

    override fun findGroupChatById(chat: UUID): Boolean {
        return (chatByChatId.containsKey(chat) && chatByChatId[chat] is GroupChat)
    }

    override fun findMessageById(messageId: UUID): Boolean {
        return messages.containsKey(messageId)
    }

    override fun getContactOfUser(userId: UUID, contactUserId: UUID): Contact? {
        return contactsOfUser[userId]?.get(contactUserId)
    }

    override fun getGroupChatIdByTag(tag: String): UUID? {
        return chats.filterIsInstance<GroupChat>().find {
            it.publicTag == tag
        }?.id
    }

    override fun getGroupChatPreferencesByChatId(userId: UUID, chatId: UUID): GroupChatPreferences? {
        return groupChatPreferences[userId]?.get(chatId)
    }

    override fun getMembersOfChat(chatId: UUID): List<UUID>? {
        if (chatByChatId.containsKey(chatId) && chatByChatId[chatId] is GroupChat) {
            return (chatByChatId[chatId] as GroupChat).members
        }
        return null
    }

    override fun getMessageById(messageId: UUID): Message? {
        return messages[messageId]
    }

    override fun isMemberOfGroupChat(userId: UUID, chatId: UUID): Boolean {
        return chatByChatId[chatId]?.hasMember(userId) ?: false
    }

    override fun isOwnerOfGroupChat(userId: UUID, chatId: UUID): Boolean {
        if (chatByChatId.containsKey(chatId) && chatByChatId[chatId] is GroupChat)
            return (chatByChatId[chatId] as GroupChat).owner == userId
        return false
    }

    private fun updateGroupChat(chat: GroupChat, newChat: GroupChat) {
        if (chatByChatId.containsKey(chat.id) && chat.id == newChat.id) {
            chats.remove(chat)
            chats.add(newChat)
            chatByChatId[chat.id] = newChat
        }
    }

    override fun joinGroupChat(userId: UUID, chatId: UUID) {
        if (chatByChatId.containsKey(chatId) && chatByChatId[chatId] is GroupChat) {
            val members = (chatByChatId[chatId] as GroupChat).members.toMutableList()
            members.add(userId)
            val chat = chatByChatId[chatId] as GroupChat
            val newChat = GroupChat(
                chat.id,
                chat.title,
                chat.owner,
                members,
                chat.avatar,
                chat.blacklist,
                chat.publicTag,
                chat.privateInviteToken
            )
            updateGroupChat(chat, newChat)
        }
    }

    override fun removeUserFromBlackListOfGroupChat(userId: UUID, chatId: UUID) {
        if (chatByChatId.containsKey(chatId) && chatByChatId[chatId] is GroupChat) {
            val blacklist = (chatByChatId[chatId] as GroupChat).blacklist.toMutableList()
            blacklist.remove(userId)
            val chat = chatByChatId[chatId] as GroupChat
            val newChat = GroupChat(
                chat.id,
                chat.title,
                chat.owner,
                chat.members,
                chat.avatar,
                blacklist,
                chat.publicTag,
                chat.privateInviteToken
            )
            updateGroupChat(chat, newChat)
        }
    }

    override fun removeUserFromGroupChat(userId: UUID, chatId: UUID) {
        if (chatByChatId.containsKey(chatId) && chatByChatId[chatId] is GroupChat) {
            val members = (chatByChatId[chatId] as GroupChat).members.toMutableList()
            members.remove(userId)
            val chat = chatByChatId[chatId] as GroupChat
            val newChat = GroupChat(
                chat.id,
                chat.title,
                chat.owner,
                members,
                chat.avatar,
                chat.blacklist,
                chat.publicTag,
                chat.privateInviteToken
            )
            updateGroupChat(chat, newChat)
        }
    }

    override fun setOwnerOfGroupChat(userId: UUID, chatId: UUID) {
        if (chatByChatId.containsKey(chatId) && chatByChatId[chatId] is GroupChat) {
            val chat = chatByChatId[chatId] as GroupChat
            val newChat = GroupChat(
                chat.id,
                chat.title,
                userId,
                chat.members,
                chat.avatar,
                chat.blacklist,
                chat.publicTag,
                chat.privateInviteToken
            )
            updateGroupChat(chat, newChat)
        }
    }

    override fun setPreferredTiTleOfGroupChat(userId: UUID, chatId: UUID, title: String) {
        val groupChatPrefs = groupChatPreferences[userId]?.get(chatId) ?: return
        val newGroupChatPreferences = GroupChatPreferences(
            groupChatPrefs.owner, groupChatPrefs.targetChat,
            title
        )
        groupChatPreferences[userId]?.set(chatId, newGroupChatPreferences)
    }

    override fun setPrivateInviteTokenOfGroupChat(inviteToken: String, chatId: UUID) {
        if (chatByChatId.containsKey(chatId) && chatByChatId[chatId] is GroupChat) {
            val chat = chatByChatId[chatId] as GroupChat
            val newChat = GroupChat(
                chat.id,
                chat.title,
                chat.owner,
                chat.members,
                chat.avatar,
                chat.blacklist,
                chat.publicTag,
                inviteToken
            )
            updateGroupChat(chat, newChat)
        }
    }

    override fun setPublicTagOfGroupChat(publicTag: String, chatId: UUID) {
        if (chatByChatId.containsKey(chatId) && chatByChatId[chatId] is GroupChat) {
            val chat = chatByChatId[chatId] as GroupChat
            val newChat = GroupChat(
                chat.id,
                chat.title,
                chat.owner,
                chat.members,
                chat.avatar,
                chat.blacklist,
                publicTag,
                chat.privateInviteToken
            )
            updateGroupChat(chat, newChat)
        }
    }

    override fun setTitleOfGroupChat(title: String, chatId: UUID) {
        if (chatByChatId.containsKey(chatId) && chatByChatId[chatId] is GroupChat) {
            val chat = chatByChatId[chatId] as GroupChat
            val newChat = GroupChat(
                chat.id,
                title,
                chat.owner,
                chat.members,
                chat.avatar,
                chat.blacklist,
                chat.publicTag,
                chat.privateInviteToken
            )
            updateGroupChat(chat, newChat)
        }
    }

    private fun updateUser(userId: UUID, username: String, newUser: User) {
        if (userById.containsKey(userId) && newUser.id == userId && newUser.username == username) {
            userById[newUser.id] = newUser
            userByUsername[newUser.username] = newUser
        }
    }

    override fun updateProfileDisplayName(userId: UUID, displayName: String) {
        val user = userById[userId] ?: return
        val newUser = User(user.id, user.username, displayName, user.email)
        updateUser(userId, user.username, newUser)
    }

    override fun updateProfileEmail(userId: UUID, email: String?) {
        val user = userById[userId] ?: return
        val newUser = User(user.id, user.username, user.displayName, email)
        updateUser(userId, user.username, newUser)
    }

    override fun withdrawPrivateInviteTokenOfGroupChat(chatId: UUID) {
        if (chatByChatId.containsKey(chatId) && chatByChatId[chatId] is GroupChat) {
            val chat = chatByChatId[chatId] as GroupChat
            val newChat = GroupChat(
                chat.id,
                chat.title,
                chat.owner,
                chat.members,
                chat.avatar,
                chat.blacklist,
                chat.publicTag,
                null
            )
            updateGroupChat(chat, newChat)
        }
    }

    override fun withdrawPublicTagOfGroupChat(chatId: UUID) {
        if (chatByChatId.containsKey(chatId) && chatByChatId[chatId] is GroupChat) {
            val chat = chatByChatId[chatId] as GroupChat
            val newChat = GroupChat(
                chat.id,
                chat.title,
                chat.owner,
                chat.members,
                chat.avatar,
                chat.blacklist,
                null,
                chat.privateInviteToken
            )
            updateGroupChat(chat, newChat)
        }
    }

    override fun addToDeletedMessages(messageId: UUID) {
        val message = messages[messageId] ?: return
        deletedMessages[messageId] = DeletedMessage(message.id, message.chat, message.date, message.sender)
    }

    override fun addUserToBlacklistOfGroupChat(userId: UUID, chatId: UUID) {
        if (chatByChatId.containsKey(chatId) && chatByChatId[chatId] is GroupChat) {
            val blacklist = (chatByChatId[chatId] as GroupChat).blacklist.toMutableList()
            blacklist.add(userId)
            val chat = chatByChatId[chatId] as GroupChat
            val newChat = GroupChat(
                chat.id,
                chat.title,
                chat.owner,
                chat.members,
                chat.avatar,
                blacklist,
                chat.publicTag,
                chat.privateInviteToken
            )
            updateGroupChat(chat, newChat)
        }
    }

    override fun getTextMessage(messageId: UUID): TextMessage? {
        val message = messages[messageId] ?: return null
        if (message is TextMessage)
            return message
        return null
    }
}