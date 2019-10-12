package snailmail.standalone.client

import snailmail.core.*
import snailmail.core.api.API
import snailmail.core.api.AuthToken
import snailmail.core.api.AuthenticationResult
import java.util.*

class ClientSideProtocolAPI : API {
    override fun authenticate(credentials: UserCredentials): AuthenticationResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun register(credentials: UserCredentials): AuthenticationResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAvailableChats(token: AuthToken): ChatRetriever {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPersonalChatWith(token: AuthToken, user: UUID): PersonalChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createGroupChat(token: AuthToken, title: String, invitedMembers: List<UUID>): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getChatMessages(token: AuthToken, chat: UUID): MessageRetriever {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendTextMessage(token: AuthToken, text: String, chat: UUID): TextMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun searchByUsername(token: AuthToken, username: String): User? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}