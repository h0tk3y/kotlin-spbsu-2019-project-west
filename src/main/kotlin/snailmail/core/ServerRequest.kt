package snailmail.core

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import snailmail.core.api.APITransportMapping
import snailmail.core.api.AuthToken
import java.util.*
import kotlin.reflect.KClass

@TypeFor(field = APITransportMapping.Convention.method, adapter = ServerRequestAdapter::class)
sealed class ServerRequest(val method: String)

data class AuthenticateRequest(val username: String, val password: String) : ServerRequest(APITransportMapping.Auth.authenticate)
data class RegisterRequest(val username: String, val password: String) : ServerRequest(APITransportMapping.Auth.register)

data class GetAvailableChatsRequest(val token: AuthToken) : ServerRequest(APITransportMapping.Chat.getAvailableChats)
data class GetPersonalChatWithRequest(val token: AuthToken, val user: UUID) : ServerRequest(APITransportMapping.Chat.getPersonalChatWith)
data class CreateGroupChatRequest(val token: AuthToken, val title: String,
                                  val invitedMembers: List<UUID>) : ServerRequest(APITransportMapping.Chat.createGroupChat)

data class GetChatMessagesRequest(val token: AuthToken, val chat: UUID) : ServerRequest(APITransportMapping.Message.getChatMessages)
data class SendTextMessageRequest(val token: AuthToken, val text: String,
                                  val chat: UUID) : ServerRequest(APITransportMapping.Message.sendTextMessage)

data class SearchByUsernameRequest(val token: AuthToken, val username: String) : ServerRequest(APITransportMapping.User.searchByUsername)
data class GetUserByIdRequest(val token: AuthToken, val id: UUID) : ServerRequest(APITransportMapping.User.getUserById)

class ServerRequestAdapter : TypeAdapter<ServerRequest> {
    override fun classFor(type: Any): KClass<out ServerRequest> = when (type as String) {
        APITransportMapping.Auth.authenticate -> AuthenticateRequest::class
        APITransportMapping.Auth.register -> RegisterRequest::class

        APITransportMapping.Chat.getAvailableChats -> GetAvailableChatsRequest::class
        APITransportMapping.Chat.getPersonalChatWith -> GetPersonalChatWithRequest::class
        APITransportMapping.Chat.createGroupChat -> CreateGroupChatRequest::class

        APITransportMapping.Message.getChatMessages -> GetChatMessagesRequest::class
        APITransportMapping.Message.sendTextMessage -> SendTextMessageRequest::class

        APITransportMapping.User.searchByUsername -> SearchByUsernameRequest::class
        APITransportMapping.User.getUserById -> GetUserByIdRequest::class

        else -> throw IllegalArgumentException("Unknown method: $type")
    }
}