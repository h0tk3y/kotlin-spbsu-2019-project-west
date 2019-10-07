package snailmail.server

import snailmail.core.*
import snailmail.core.api.*
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

class Server : API {
    var userCredentials = HashMap<String, String>()
    var chats = mutableListOf<Chat>()
    var usernames = HashMap<String, UUID>()
    var idByToken = HashMap<String, UUID>()
    var messsages = HashMap<Chat, List<Message>>()

    override fun authenticate(credentials: UserCredentials): AuthenticationResult {
        return if (userCredentials.contains(credentials.username) &&
                userCredentials[credentials.username] == credentials.password)
            AuthSuccessful("")
        else AuthWrongCredentials()
    }

    override fun register(credentials: UserCredentials): AuthenticationResult {
        return if (userCredentials.contains(credentials.username)) AuthRegisterFailed("");
        else {
            userCredentials[credentials.username] = credentials.password
            usernames[credentials.username] = UUID.randomUUID()
            AuthSuccessful("")
        }
    }

    override fun getAvailableChats(token: AuthToken): ChatRetriever {
        if (idByToken[token] == null)
            throw Exception("((")
        return object : ChatRetriever {
            override fun getChats() : List<Chat> {
                return this@Server.chats.filter { it.hasMember(idByToken[token]!!) };
            }
        }
    }

    override fun getPersonalChatWith(token: AuthToken, user: UUID): PersonalChat {
        if (idByToken[token] == null)
            throw Exception("((")
        val res = chats.find { when (it) {
            is PersonalChat -> it.person1 == idByToken[token] && it.person2 == user
            else -> false
        } }
        return if (res == null) {
            val chat : PersonalChat = PersonalChat(UUID.randomUUID(), idByToken[token]!!, user)
            chats.add(chat)
            chat
        } else res as PersonalChat
    }

    override fun getChatMessages(token: AuthToken, chat: UUID): MessageRetriever {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendTextMessage(token: AuthToken, text: String, chat: UUID): TextMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createGroupChat(token: AuthToken, title: String, invitedMembers: List<UUID>): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun searchByUsername(token: AuthToken, username: String): User? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}