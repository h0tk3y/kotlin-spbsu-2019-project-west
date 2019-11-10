package snailmail.core

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import snailmail.core.api.ApiTransportMapping
import snailmail.core.api.AuthToken
import java.util.*
import kotlin.reflect.KClass

@TypeFor(field = ApiTransportMapping.Convention.method, adapter = ServerRequestAdapter::class)
sealed class ServerRequest(val method: String)

data class AuthenticateRequest(val username: String, val password: String) : ServerRequest(ApiTransportMapping.Auth.authenticate.method)
data class RegisterRequest(val username: String, val password: String) : ServerRequest(ApiTransportMapping.Auth.register.method)

data class GetAvailableChatsRequest(val token: AuthToken) : ServerRequest(ApiTransportMapping.Chat.getAvailableChats.method)
data class GetPersonalChatWithRequest(val token: AuthToken, val user: UUID) : ServerRequest(ApiTransportMapping.Chat.getPersonalChatWith.method)
data class CreateGroupChatRequest(val token: AuthToken, val title: String,
                                  val invitedMembers: List<UUID>) : ServerRequest(ApiTransportMapping.Chat.createGroupChat.method)

data class GetChatMessagesRequest(val token: AuthToken, val chat: UUID) : ServerRequest(ApiTransportMapping.Message.getChatMessages.method)
data class SendTextMessageRequest(val token: AuthToken, val text: String,
                                  val chat: UUID) : ServerRequest(ApiTransportMapping.Message.sendTextMessage.method)

data class GetUserByUsernameRequest(val token: AuthToken, val username: String) : ServerRequest(ApiTransportMapping.User.searchByUsername.method)
data class GetUserByIdRequest(val token: AuthToken, val id: UUID) : ServerRequest(ApiTransportMapping.User.getUserById.method)

class ServerRequestAdapter : TypeAdapter<ServerRequest> {
    override fun classFor(type: Any): KClass<out ServerRequest> = when (type as String) {
        ApiTransportMapping.Auth.authenticate.method -> AuthenticateRequest::class
        ApiTransportMapping.Auth.register.method -> RegisterRequest::class

        ApiTransportMapping.Chat.getAvailableChats.method -> GetAvailableChatsRequest::class
        ApiTransportMapping.Chat.getPersonalChatWith.method -> GetPersonalChatWithRequest::class
        ApiTransportMapping.Chat.createGroupChat.method -> CreateGroupChatRequest::class

        ApiTransportMapping.Message.getChatMessages.method -> GetChatMessagesRequest::class
        ApiTransportMapping.Message.sendTextMessage.method -> SendTextMessageRequest::class

        ApiTransportMapping.User.searchByUsername.method -> GetUserByUsernameRequest::class
        ApiTransportMapping.User.getUserById.method -> GetUserByIdRequest::class

        else -> throw IllegalArgumentException("Unknown method: $type")
    }
}