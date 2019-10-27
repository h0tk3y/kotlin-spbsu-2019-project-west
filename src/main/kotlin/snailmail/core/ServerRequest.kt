package snailmail.core

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import snailmail.core.api.APITransportMapping
import snailmail.core.api.AuthToken
import java.util.*
import kotlin.reflect.KClass

@TypeFor(field = APITransportMapping.Convention.method, adapter = ServerRequestAdapter::class)
sealed class ServerRequest(val method: String)

data class AuthenticateRequest(val username: String, val password: String) : ServerRequest(APITransportMapping.Auth.authenticate.method)
data class RegisterRequest(val username: String, val password: String) : ServerRequest(APITransportMapping.Auth.register.method)

data class GetAvailableChatsRequest(val token: AuthToken) : ServerRequest(APITransportMapping.Chat.getAvailableChats.method)
data class GetPersonalChatWithRequest(val token: AuthToken, val user: UUID) : ServerRequest(APITransportMapping.Chat.getPersonalChatWith.method)
data class CreateGroupChatRequest(val token: AuthToken, val title: String,
                                  val invitedMembers: List<UUID>) : ServerRequest(APITransportMapping.Chat.createGroupChat.method)

data class GetChatMessagesRequest(val token: AuthToken, val chat: UUID) : ServerRequest(APITransportMapping.Message.getChatMessages.method)
data class SendTextMessageRequest(val token: AuthToken, val text: String,
                                  val chat: UUID) : ServerRequest(APITransportMapping.Message.sendTextMessage.method)

data class SearchByUsernameRequest(val token: AuthToken, val username: String) : ServerRequest(APITransportMapping.User.searchByUsername.method)
data class GetUserByIdRequest(val token: AuthToken, val id: UUID) : ServerRequest(APITransportMapping.User.getUserById.method)

class ServerRequestAdapter : TypeAdapter<ServerRequest> {
    override fun classFor(type: Any): KClass<out ServerRequest> = when (type as String) {
        APITransportMapping.Auth.authenticate.method -> AuthenticateRequest::class
        APITransportMapping.Auth.register.method -> RegisterRequest::class

        APITransportMapping.Chat.getAvailableChats.method -> GetAvailableChatsRequest::class
        APITransportMapping.Chat.getPersonalChatWith.method -> GetPersonalChatWithRequest::class
        APITransportMapping.Chat.createGroupChat.method -> CreateGroupChatRequest::class

        APITransportMapping.Message.getChatMessages.method -> GetChatMessagesRequest::class
        APITransportMapping.Message.sendTextMessage.method -> SendTextMessageRequest::class

        APITransportMapping.User.searchByUsername.method -> SearchByUsernameRequest::class
        APITransportMapping.User.getUserById.method -> GetUserByIdRequest::class

        else -> throw IllegalArgumentException("Unknown method: $type")
    }
}