package snailmail.server.data

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import snailmail.core.*
import java.util.*
import org.joda.time.DateTime

class MySQL() : DataBase {
    private val url = "jdbc:h2:mem:test;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1"

    private object Users : Table() {
        val id = uuid("id").primaryKey()
        val username = varchar("username", 256) references UsersCredentials.username
    }

    private object UsersCredentials : Table() {
        val username = varchar("username", 256).primaryKey()
        val password = varchar("password", 50)
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
        val publicTag = text("publicTag")
        val privateInviteToken = text("privateInviteToken")
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
    }

    private object TextMessages : Table() {
        val id = uuid("id").primaryKey()
        val chat = uuid("chat")
        val date = date("date")
        val sender = uuid("sender")
        val content = text("content")
        val edited = bool("edited")
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

    override fun getChats(userId: UUID): List<Chat> {
        val chats = transaction {
            ChatsOfUsers.select { ChatsOfUsers.id eq userId }.limit(1, offset = 0).map { row ->
                deserializeUUIDs(row[ChatsOfUsers.chats]).mapNotNull { (getChatByChatId(it)) }
            }
        }
        if (chats.isEmpty())
            return listOf()
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

    private fun getTextMessage(id: UUID): TextMessage? {
        Database.connect(url, driver = "org.h2.Driver")
        val message = transaction {
            TextMessages.select { TextMessages.id eq id }.limit(1, offset = 0).map {
                TextMessage(
                    it[TextMessages.id], id, it[TextMessages.date].toDate(),
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
                var chats = getChats(id).map { it.id }
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

    override fun getGroupChatPreferencesByChatId(chatId: UUID): GroupChatPreferences? {
        Database.connect(url, driver = "org.h2.Driver")
        val groupChatPreferences = transaction {
            GroupChatsPreferences.select { GroupChatsPreferences.targetChat eq chatId }.limit(1, 0).map {
                snailmail.core.GroupChatPreferences(it[GroupChatsPreferences.owner], it[GroupChatsPreferences.targetChat],
                    it[GroupChatsPreferences.title])
            }
        }
        if (groupChatPreferences.isEmpty())
            return null
        return groupChatPreferences[0]
    }

    override fun isMemberOfGroupChat(userId: UUID, chatId: UUID): Boolean {
        TODO("not implemented")
    }

    override fun isOwnerOfGroupChat(userId: UUID, chatId: UUID): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun joinGroupChat(userId: UUID, chatId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setTitleOfGroupChat(title: String, chatId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setOwnerOfGroupChat(userId: UUID, chatId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setPublicTagOfGroupChat(publicTag: String, chatId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setPrivateInviteTokenOfGroupChat(inviteToken: String, chatId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setPreferredTiTleOfGroupChat(userId: UUID, chatId: UUID, title: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeUserFromGroupChat(userId: UUID, chatId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeUserFromBlackListOfGroupChat(userId: UUID, chatId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun withdrawPublicTagOfGroupChat(chatId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun withdrawPrivateInviteTokenOfGroupChat(chatId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMembersOfChat(chatId: UUID): List<UUID>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMessageById(messageId: UUID): Message? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteMessage(messageId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun editTextMessage(messageId: UUID, text: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTextMessageById(messageId: UUID): TextMessage? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findMessageById(messageId: UUID): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContactOfUser(userId: UUID, contactUserId: UUID): Contact? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun changeContactDisplayName(userId: UUID, targetUserId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun changeBannedContactOfUser(userId: UUID, targetUserId: UUID, isBanned: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateProfileDisplayName(userId: UUID, displayName: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateProfileEmail(userId: UUID, email: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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