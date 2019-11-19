package snailmail.server.data

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import snailmail.core.*
import java.util.*
import org.joda.time.DateTime

class MySQL() : DataBase {
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

    private fun serializeUUIDs(ids: List<UUID>): String {
        var res = ""
        for (id in ids) {
            res += "$id//"
        }
        return res.substring(0, res.length - 2)
    }

    private fun deserializeUUIDs(str: String): List<UUID> {
        val res = mutableListOf<UUID>()
        str.split("//").forEach {
            res.add(UUID.fromString(it))
        }
        return res
    }

    override fun verifyUserCredentials(username: String, password: String): Boolean {
        val userCredentials = transaction {
            UsersCredentials.select { (UsersCredentials.username eq username) and (UsersCredentials.password eq password) }
                .limit(1, offset = 0)
        }
        return !userCredentials.empty()
    }

    override fun getUserByUsername(username: String): User? {
        val user = transaction {
            Users.select { Users.username eq username }.limit(1, offset = 0).map {
                User(it[Users.id], it[Users.username], it[Users.username], null, null)}
        }
        if (user.isEmpty())
            return null
        return user[0]
    }

    override fun getUserById(id: UUID): User? {
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
        val type = transaction {
            Chats.select { Chats.id eq id }.limit(1, offset = 0).map { it[Chats.type] }
        }
        if (type.isEmpty())
            return null
        return type[0]
    }

    private fun getPersonalChat(id: UUID): PersonalChat? {
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
        val typeOfChat = getTypeOfChat(id) ?: return null

        if (typeOfChat == "personal")
            return getPersonalChat(id)
        else if (typeOfChat == "group")
            return getGroupChat(id)
        return null
    }

    override fun getPersonalChatWith(thisUser: UUID, otherUser: UUID): PersonalChat? {
        val personalChatWith = transaction {
            PersonalChats.select { (PersonalChats.person1 eq thisUser) and (PersonalChats.person2 eq otherUser) or (PersonalChats.person1 eq otherUser) and (PersonalChats.person2 eq thisUser) }
                .limit(0, offset = 1).map {
                    PersonalChat(it[PersonalChats.id], thisUser, otherUser)
                }
        }
        if (personalChatWith.isEmpty())
            return null
        return personalChatWith[0]
    }

    private fun getMessageType(id: UUID): String? {
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
        val textMessages = transaction {
            MessagesOfChats.select { MessagesOfChats.chatId eq id }.limit(1, offset = 0).map {
                deserializeUUIDs(it[MessagesOfChats.messages]).mapNotNull {
                    if (getMessageType(id) == "text")
                        getTextMessage(id)
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
    }

    private fun updateChatsOfUser(id: UUID, chatId: UUID) {
        transaction {
            ChatsOfUsers.update({ ChatsOfUsers.id eq id }) { row ->
                val chats = getChats(id).map { it.id }
                chats.toMutableList().add(chatId)
                row[ChatsOfUsers.chats] = serializeUUIDs(chats)
            }
        }
    }

    private fun addMessagesOfChat(chatId: UUID) {
        transaction {
            MessagesOfChats.insert {
                it[MessagesOfChats.chatId] = chatId
                it[messages] = ""
            }
        }
    }

    private fun addPersonalChat(chat: PersonalChat) {
        transaction {
            PersonalChats.insert {
                it[id] = chat.id
                it[person1] = chat.person1
                it[person2] = chat.person2
            }
        }

        updateChatsOfUser(chat.person1, chat.id)
        updateChatsOfUser(chat.person2, chat.id)
    }

    private fun addGroupChat(chat: GroupChat) {
        transaction {
            GroupChats.insert {
                it[id] = chat.id
                it[title] = chat.title
                it[owner] = chat.owner
                it[members] = serializeUUIDs(chat.members)
            }
        }

        val members = getMembersOfGroupChat(chat) ?: return
        for (member in members)
            updateChatsOfUser(member, chat.id)
    }

    override fun addChat(chat: Chat) {
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
        transaction {
            MessagesOfChats.update({MessagesOfChats.chatId eq chatId}) { row ->
                val messages = getMessagesByChatId(chatId)?.map { it.id } ?: return@update
                messages.toMutableList().add(messageId)
                row[MessagesOfChats.messages] = serializeUUIDs(messages)
            }
        }
    }

    private fun addTextMessage(chat: UUID, message: TextMessage) {
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

    //only TextMessages :(
    override fun addMessage(chat: UUID, message: Message) {
        if (message !is TextMessage)
            return
        addTextMessage(chat, message)
        addMessageToChat(message.id, chat)
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
}