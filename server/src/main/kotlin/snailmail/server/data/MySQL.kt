package snailmail.server.data

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import snailmail.core.*
import java.util.*

class MySQL() : DataBase {
    private val url = "jdbc:h2:mem:test;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1"

    private object Users : Table() {
        val id = uuid("id").primaryKey()
        val username = varchar("username", 256) references UsersCredentials.username
        val displayName = varchar("displayName", 256)
        val email = text("email").nullable()
    }

    private object UsersCredentials : Table() {
        val username = varchar("username", 256).primaryKey()
        val password = varchar("password", 50)
    }

    private object Contacts : Table() {
        val owner = uuid("owner") references ContactsOfUser.owner
        val targetUser = uuid("targetUser")
        val displayName = varchar("displayName", 256).nullable()
        val banned = bool("banned")
    }

    private object ContactsOfUser: Table() {
        val owner = uuid("owner").primaryKey()
        val contacts = text("contacts")
    }

    private object Chats : Table() {
        val type = varchar("type", 10)
        val id = uuid("id").primaryKey()
    }

    private object PersonalChats : Table() {
        val id = uuid("id") references Chats.id
        val person1 = uuid("person1")
        val person2 = uuid("person2")
    }

    private object GroupChats : Table() {
        val id = uuid("id") references Chats.id
        val title = varchar("title", 256)
        val owner = uuid("owner")
        val members = text("members")
        val blacklist = text("blacklist")
        val publicTag = text("publicTag").nullable()
        val privateInviteToken = text("privateInviteToken").nullable()
    }

    private object GroupChatsPreferences : Table() {
        val owner = uuid("owner")
        val targetChat = uuid("targetChat") references GroupChats.id
        val title = varchar("title", 256)
    }

    private object Messages : Table() {
        val id = uuid("id").primaryKey()
        val type = varchar("type", 10)
        val chat = uuid("chat") references Chats.id
        val date = date("date")
        val sender = uuid("sender")
    }

    private object TextMessages : Table() {
        val id = uuid("id").primaryKey() references Messages.id
        val chat = uuid("chat") references Messages.chat
        val date = date("date") references Messages.date
        val content = text("content")
        val edited = bool("edited")
        val sender = uuid("sender") references Messages.sender
    }

    private object DeletedMessages : Table() {
        val id = uuid("id").primaryKey()
        val chat = uuid("chat") references Chats.id
        val date = date("date")
        val sender = uuid("sender")
    }

    private object ChatsOfUsers : Table() {
        val id = uuid("id").primaryKey()
        val chats = text("chats")
    }

    private object MessagesOfChats : Table() {
        val chatId = uuid("chatId") references Chats.id
        val messages = text("messages")
    }

    init {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(Users, UsersCredentials, Chats, PersonalChats, GroupChats,
                Messages, TextMessages, ChatsOfUsers, MessagesOfChats)
        }
    }

    private fun serializeUUIDs(ids: List<UUID>): String {
        var res = ""
        for (id in ids) {
            res += "$id//"
        }
        if (res == "")
            return res
        return res.substring(0, res.length - 2)
    }

    private fun deserializeUUIDs(str: String): List<UUID> {
        val res = mutableListOf<UUID>()
        if (str == "")
            return res
        str.split("//").forEach {
            res.add(UUID.fromString(it))
        }
        return res
    }

    override fun verifyUserCredentials(username: String, password: String): Boolean {
        Database.connect(url, driver = "org.h2.Driver")
        val userCredentials = transaction {
            UsersCredentials.select { (UsersCredentials.username eq username) and (UsersCredentials.password eq password) }
                .limit(1, offset = 0).toList()
        }
        return userCredentials.isNotEmpty()
    }

    override fun getUserByUsername(username: String): User? {
        Database.connect(url, driver = "org.h2.Driver")
        val user = transaction {
            Users.select { Users.username eq username }.limit(1, offset = 0).map {
                User(it[Users.id], it[Users.username], it[Users.username], null, null)}
        }
        if (user.isEmpty())
            return null
        return user[0]
    }

    override fun getUserById(id: UUID): User? {
        Database.connect(url, driver = "org.h2.Driver")
        val user = transaction {
            Users.select { Users.id eq id }.limit(1, offset = 0).map {
                User(id, it[Users.username], it[Users.username], null, null)
            }
        }
        if (user.isEmpty())
            return null
        return user[0]
    }

    private fun getChatsIds(userId: UUID): List<UUID>? {
        val chats = transaction {
            ChatsOfUsers.select { ChatsOfUsers.id eq userId }.limit(1, offset = 0).map { row ->
                deserializeUUIDs(row[ChatsOfUsers.chats])
            }
        }
        if (chats.isEmpty())
            return null
        return chats[0]
    }

    override fun getChats(userId: UUID): List<Chat>? {
        val chats = transaction {
            ChatsOfUsers.select { ChatsOfUsers.id eq userId }.limit(1, offset = 0).map { row ->
                deserializeUUIDs(row[ChatsOfUsers.chats]).mapNotNull { (getChatByChatId(it)) }
            }
        }
        if (chats.isEmpty())
            return null
        return chats[0]
    }

    private fun getTypeOfChat(id: UUID): String? {
        Database.connect(url, driver = "org.h2.Driver")
        val type = transaction {
            Chats.select { Chats.id eq id }.limit(1, offset = 0).map { it[Chats.type] }
        }
        if (type.isEmpty())
            return null
        return type[0]
    }

    private fun getPersonalChat(id: UUID): PersonalChat? {
        Database.connect(url, driver = "org.h2.Driver")
        val personalChat = transaction {
            PersonalChats.select { PersonalChats.id eq id }.limit(1, offset = 0).map {
            PersonalChat(
                id, it[PersonalChats.person1],
                it[PersonalChats.person2]
            )
            }
        }
        if (personalChat.isEmpty())
            return null
        return personalChat[0]
    }

    //add new fields
    private fun getGroupChat(id: UUID): GroupChat? {
        Database.connect(url, driver = "org.h2.Driver")
        val groupChat = transaction {
            GroupChats.select { GroupChats.id eq id }.limit(0, offset = 1).map {
                GroupChat(
                    id, it[GroupChats.title],
                    it[GroupChats.owner], deserializeUUIDs(it[GroupChats.members])
                )
            }
        }
        if (groupChat.isEmpty())
            return null
        return groupChat[0]
    }

    private fun getMembersOfGroupChat(chat: GroupChat): List<UUID>? {
        Database.connect(url, driver = "org.h2.Driver")
        val members = transaction {
            GroupChats.select { GroupChats.id eq chat.id }.limit(1, 0).map {
                deserializeUUIDs(it[GroupChats.members])
            }
        }
        if (members.isEmpty())
            return null
        return members[0]
    }

    override fun getChatByChatId(id: UUID): Chat? {
        Database.connect(url, driver = "org.h2.Driver")
        val typeOfChat = getTypeOfChat(id) ?: return null
        if (typeOfChat == "personal")
            return getPersonalChat(id)
        else if (typeOfChat == "group")
            return getGroupChat(id)
        return null
    }

    override fun getPersonalChatWith(thisUser: UUID, otherUser: UUID): PersonalChat? {
        Database.connect(url, driver = "org.h2.Driver")
        val personalChatWith = transaction {
            PersonalChats.select { ((PersonalChats.person1 eq thisUser) and (PersonalChats.person2 eq otherUser)) or ((PersonalChats.person1 eq otherUser) and (PersonalChats.person2 eq thisUser)) }
                .limit(0, offset = 1).map {
                    PersonalChat(it[PersonalChats.id], it[PersonalChats.person1], it[PersonalChats.person2])
                }
        }
        if (personalChatWith.isEmpty())
            return null
        return personalChatWith[0]
    }

    private fun getMessageType(id: UUID): String? {
        Database.connect(url, driver = "org.h2.Driver")
        val type = transaction {
            Messages.select { Messages.id eq id }.limit(1, offset = 0).map {
                it[Messages.type]
            }
        }
        if (type.isEmpty())
            return null
        return type[0]
    }

    override fun getTextMessage(messageId: UUID): TextMessage? {
        Database.connect(url, driver = "org.h2.Driver")
        val message = transaction {
            TextMessages.select { TextMessages.id eq messageId }.limit(1, offset = 0).map {
                TextMessage(
                    it[TextMessages.id], messageId, it[TextMessages.date].toDate(),
                    it[TextMessages.sender], it[TextMessages.content],
                    it[TextMessages.edited]
                )
            }
        }
        if (message.isEmpty())
            return null
        return message[0]
    }

    // finds only text messages :(
    override fun getMessagesByChatId(id: UUID): List<Message>? {
        Database.connect(url, driver = "org.h2.Driver")
        val textMessages = transaction {
            MessagesOfChats.select { MessagesOfChats.chatId eq id }.limit(1, offset = 0).map { row ->
                deserializeUUIDs(row[MessagesOfChats.messages]).mapNotNull {
                    if (getMessageType(it) == "text")
                        getTextMessage(it)
                    else
                        null
                }
            }
        }
        if (textMessages.isEmpty())
            return null
        return textMessages[0]
    }

    override fun addUserCredentials(username: String, password: String) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            UsersCredentials.insert {
                it[UsersCredentials.username] = username
                it[UsersCredentials.password] = password
            }
        }
    }

    override fun addUser(user: User) {
        transaction {
            Users.insert {
                it[id] = user.id
                it[username] = user.username
                it[displayName] = user.displayName
                it[email] = user.email
            }
        }
        addChatsOfUser(user.id)
    }

    private fun addChatsOfUser(id: UUID) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            ChatsOfUsers.insert {
                it[ChatsOfUsers.id] = id
                it[ChatsOfUsers.chats] = ""
            }
        }
    }

    private fun updateChatsOfUser(id: UUID, chatId: UUID) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            ChatsOfUsers.update({ ChatsOfUsers.id eq id }) { row ->
                var chats = getChats(id)?.map { it.id } ?: return@update
                chats += chatId
                val str = serializeUUIDs(chats)
                row[ChatsOfUsers.chats] = str
            }
        }
    }

    private fun addMessagesOfChat(chatId: UUID) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            MessagesOfChats.insert {
                it[MessagesOfChats.chatId] = chatId
                it[messages] = ""
            }
        }
    }

    private fun addPersonalChat(chat: PersonalChat) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            PersonalChats.insert {
                it[id] = chat.id
                it[person1] = chat.person1
                it[person2] = chat.person2
            }
        }
        updateChatsOfUser(chat.person1, chat.id)
        if (chat.person2 != chat.person1)
            updateChatsOfUser(chat.person2, chat.id)
    }

    private fun addGroupChat(chat: GroupChat) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            GroupChats.insert {
                it[id] = chat.id
                it[title] = chat.title
                it[owner] = chat.owner
                it[members] = serializeUUIDs(chat.members)
                it[blacklist] = ""
                it[publicTag] = ""
                it[privateInviteToken] = ""
            }
        }
        updateChatsOfUser(chat.owner, chat.id)
        val members = getMembersOfGroupChat(chat) ?: return
        for (member in members)
            updateChatsOfUser(member, chat.id)
    }

    override fun addChat(chat: Chat) {
        Database.connect(url, driver = "org.h2.Driver")
        if (chat !is PersonalChat && chat !is GroupChat)
            return
        transaction {
            Chats.insert {
                it[id] = chat.id
                it[type] = chat.type
            }
        }
        if (chat is PersonalChat)
            addPersonalChat(chat)
        else if (chat is GroupChat)
            addGroupChat(chat)
        addMessagesOfChat(chat.id)
    }

    private fun addMessageToChat(messageId: UUID, chatId: UUID) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            MessagesOfChats.update({MessagesOfChats.chatId eq chatId}) { row ->
                val messages = getMessagesByChatId(chatId)?.map { it.id }?.toMutableList() ?: return@update
                messages.add(messageId)
                row[MessagesOfChats.messages] = serializeUUIDs(messages)
            }
        }
    }

    private fun addTextMessage(chat: UUID, message: TextMessage) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            TextMessages.insert {
                it[id] = message.id
                it[TextMessages.chat] = chat
                it[date] = DateTime(message.date)
                it[sender] = message.sender
                it[content] = message.content
                it[edited] = message.edited
            }
        }
    }

    private fun addMessageToMessages(message: Message) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            Messages.insert {
                it[id] = message.id
                it[type] = message.type
                it[chat] = message.chat
                it[date] = DateTime(message.date)
                it[sender] = message.sender
            }
        }
    }

    //only TextMessages :(
    override fun addMessage(chat: UUID, message: Message) {
        addMessageToChat(message.id, chat)
        addMessageToMessages(message)
        if (message !is TextMessage) {
            return
        }
        addTextMessage(chat, message)
    }

    override fun deleteChat(id: UUID) {
    }

    override fun findUserById(id: UUID): Boolean {
        return getUserById(id) != null
    }

    override fun findUsername(username: String): Boolean {
        return getUserByUsername(username) != null
    }

    override fun findMessagesByChatId(chat: UUID): Boolean {
        return getMessagesByChatId(chat) != null
    }

    override fun changePassword(credentials: UserCredentials): Boolean {
        Database.connect(url, driver = "org.h2.Driver")
        var isSuccess = false
        transaction {
            UsersCredentials.update({ UsersCredentials.username eq credentials.username }) {
                row -> row[password] = credentials.password
                isSuccess = true
                return@update
            }
        }
        return isSuccess
    }


    //how to find item normally???
    override fun findGroupChatById(chat: UUID): Boolean {
        Database.connect(url, driver = "org.h2.Driver")
        val groupChat = transaction {
            GroupChats.select {GroupChats.id eq chat}.toList()
        }
        return groupChat.isNotEmpty()
    }


    override fun getGroupChatIdByTag(tag: String): UUID? {
        Database.connect(url, driver = "org.h2.Driver")
        val groupChatId = transaction {
            GroupChats.select {GroupChats.publicTag eq tag}.map {it[GroupChats.id]}
        }
        if (groupChatId.isEmpty())
            return null
        return groupChatId[0]
    }

    override fun getGroupChatPreferencesByChatId(userId: UUID, chatId: UUID): GroupChatPreferences? {
        Database.connect(url, driver = "org.h2.Driver")
        val groupChatPreferences = transaction {
            GroupChatsPreferences.select { GroupChatsPreferences.targetChat eq chatId }.limit(1, 0).map {
                GroupChatPreferences(it[GroupChatsPreferences.owner], it[GroupChatsPreferences.targetChat],
                    it[GroupChatsPreferences.title])
            }
        }
        if (groupChatPreferences.isEmpty())
            return null
        return groupChatPreferences[0]
    }

    override fun isMemberOfGroupChat(userId: UUID, chatId: UUID): Boolean {
        return getMembersOfGroupChat(getGroupChat(chatId) ?: return false)?.contains(userId) ?: false
    }

    override fun isOwnerOfGroupChat(userId: UUID, chatId: UUID): Boolean {
        return (getGroupChat(chatId)?.owner ?: return false) == userId
    }

    override fun joinGroupChat(userId: UUID, chatId: UUID) {
        Database.connect(url, driver = "org.h2.Driver")
        val groupChat = getGroupChat(chatId) ?: return
        val members = getMembersOfGroupChat(groupChat)?.toMutableList() ?: return
        members.add(userId)
        val chats = getChats(userId)?.map {it.id}?.toMutableList() ?: return
        chats.add(chatId)
        transaction {
            GroupChats.update({ GroupChats.id eq chatId }) {
                it[GroupChats.members] = serializeUUIDs(members)
            }
            ChatsOfUsers.update({ChatsOfUsers.id eq userId}) {
                it[ChatsOfUsers.chats] = serializeUUIDs(chats)
            }
        }
    }

    override fun setTitleOfGroupChat(title: String, chatId: UUID) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            GroupChats.update({ GroupChats.id eq chatId }) {
                it[GroupChats.title] = title
            }
        }
    }

    override fun setOwnerOfGroupChat(userId: UUID, chatId: UUID) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            GroupChats.update({ GroupChats.id eq chatId }) {
                it[GroupChats.owner] = userId
            }
        }
    }

    override fun setPublicTagOfGroupChat(publicTag: String, chatId: UUID) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            GroupChats.update({ GroupChats.id eq chatId }) {
                it[GroupChats.publicTag] = publicTag
            }
        }
    }

    override fun setPrivateInviteTokenOfGroupChat(inviteToken: String, chatId: UUID) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            GroupChats.update({ GroupChats.id eq chatId }) {
                it[GroupChats.privateInviteToken] = inviteToken
            }
        }
    }

    override fun setPreferredTiTleOfGroupChat(userId: UUID, chatId: UUID, title: String) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            GroupChatsPreferences.update({ (GroupChatsPreferences.targetChat eq chatId) and (GroupChatsPreferences.owner eq userId)}) {
                it[GroupChatsPreferences.title] = title
            }
        }
    }

    private fun getBlacklistOfGroupChat(chatId: UUID): List<UUID>? {
        Database.connect(url, driver = "org.h2.Driver")
        return transaction {
            GroupChats.select { GroupChats.id eq chatId }.singleOrNull()?.get(GroupChats.blacklist)?.let {
                deserializeUUIDs(it)
            }
        }
    }

    override fun addUserToBlacklistOfGroupChat(userId: UUID, chatId: UUID) {
        Database.connect(url, driver = "org.h2.Driver")
        val blacklist = getGroupChat(chatId)?.blacklist?.toMutableList() ?: return
        blacklist.add(userId)
        transaction {
            GroupChats.update({ GroupChats.id eq chatId }) {
                it[GroupChats.blacklist] = serializeUUIDs(blacklist)
            }
        }
    }

    override fun removeUserFromGroupChat(userId: UUID, chatId: UUID) {
        Database.connect(url, driver = "org.h2.Driver")
        getGroupChat(chatId)?.let { getMembersOfGroupChat(it) }?.toMutableList()?.let { members ->
            members.remove(userId)
            transaction {
                GroupChats.update({ GroupChats.id eq chatId }) {
                    it[GroupChats.members] = serializeUUIDs(members)
                }
            }
        }
        getChatsIds(userId)?.toMutableList()?.let {chats ->
            transaction {
                ChatsOfUsers.update({ ChatsOfUsers.id eq userId }) {
                    chats.remove(chatId)
                    it[ChatsOfUsers.chats] = serializeUUIDs(chats)
                }
            }
        }
    }

    override fun removeUserFromBlackListOfGroupChat(userId: UUID, chatId: UUID) {
        Database.connect(url, driver = "org.h2.Driver")
        getBlacklistOfGroupChat(chatId)?.toMutableList()?.let { blacklist ->
            blacklist.remove(userId)
            transaction {
                GroupChats.update({ GroupChats.id eq chatId}) {
                    it[GroupChats.blacklist] = serializeUUIDs(blacklist)
                }
            }
        }
    }

    override fun withdrawPublicTagOfGroupChat(chatId: UUID) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            GroupChats.update ({ GroupChats.id eq chatId}) {
                it[GroupChats.publicTag] = null
            }
        }
    }

    override fun withdrawPrivateInviteTokenOfGroupChat(chatId: UUID) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            GroupChats.update ({ GroupChats.id eq chatId}) {
                it[GroupChats.privateInviteToken] = null
            }
        }
    }

    override fun getMembersOfChat(chatId: UUID): List<UUID>? {
        Database.connect(url, driver = "org.h2.Driver")
        return transaction {
            GroupChats.select { GroupChats.id eq chatId }.singleOrNull()?.let { row ->
                deserializeUUIDs(row[GroupChats.members])
            }
        }
    }

    override fun getMessageById(messageId: UUID): Message? {
        Database.connect(url, driver = "org.h2.Driver")
        val type = getMessageType(messageId)
        if (type == "text")
            return getTextMessage(messageId)
        return null
    }

    override fun addToDeletedMessages(messageId: UUID) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            Messages.select { Messages.id eq messageId }.singleOrNull()?.let { row ->
                DeletedMessages.insert {
                    it[id] = row[Messages.id]
                    it[chat] = row[Messages.chat]
                    it[date] = row[Messages.date]
                    it[sender] = row[Messages.sender]
                }
            }
        }
    }

    override fun deleteMessage(messageId: UUID) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            Messages.deleteWhere { Messages.id eq messageId }
        }
    }

    override fun editTextMessage(messageId: UUID, text: String) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            TextMessages.update({ TextMessages.id eq messageId }) {
                it[content] = text
                it[edited] = true
            }
        }
    }

    override fun findMessageById(messageId: UUID): Boolean {
        Database.connect(url, driver = "org.h2.Driver")
        return transaction {
            Messages.select { Messages.id eq messageId }.singleOrNull()
        } != null
    }

    override fun getContactOfUser(userId: UUID, contactUserId: UUID): Contact? {
        Database.connect(url, driver = "org.h2.Driver")
        return transaction {
            Contacts.select { (Contacts.owner eq userId) and (Contacts.targetUser eq contactUserId) }.singleOrNull()?.let {
                Contact(it[Contacts.owner], it[Contacts.targetUser], it[Contacts.displayName], it[Contacts.banned])
            }
        }
    }

    override fun changeContactDisplayName(userId: UUID, targetUserId: UUID, newDisplayName: String) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            Contacts.update ({ (Contacts.owner eq userId) and (Contacts.targetUser eq targetUserId)}) {
                it[displayName] = newDisplayName
            }
        }
    }

    override fun changeBannedContactOfUser(userId: UUID, targetUserId: UUID, isBanned: Boolean) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            Contacts.update ({ (Contacts.owner eq userId) and (Contacts.targetUser eq targetUserId)}) {
                it[banned] = isBanned
            }
        }
    }

    override fun updateProfileDisplayName(userId: UUID, displayName: String) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            Users.update ({ Users.id eq userId }) {
                it[Users.displayName] = displayName
            }
        }
    }

    override fun updateProfileEmail(userId: UUID, email: String?) {
        Database.connect(url, driver = "org.h2.Driver")
        transaction {
            Users.update ({ Users.id eq userId }) {
                it[Users.email] = email
            }
        }
    }

    companion object {
        fun deleteDB() {
            Database.connect("jdbc:h2:mem:test;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
            transaction {
                SchemaUtils.drop(
                    Users, UsersCredentials, Chats, PersonalChats, GroupChats,
                    Messages, TextMessages, ChatsOfUsers, MessagesOfChats
                )
            }
        }
    }
}