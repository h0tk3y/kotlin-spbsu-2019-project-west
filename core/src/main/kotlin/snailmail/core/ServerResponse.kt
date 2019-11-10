package snailmail.core

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import snailmail.core.api.ApiTransportMapping
import snailmail.core.api.AuthToken
import kotlin.reflect.KClass

@TypeFor(field = ApiTransportMapping.Convention.method, adapter = ServerResponseAdapter::class)
sealed class ServerResponse(val method: String)


data class AuthenticateResponse(val result: AuthToken) : ServerResponse(ApiTransportMapping.Auth.authenticate.method)
data class RegisterResponse(val result: AuthToken) : ServerResponse(ApiTransportMapping.Auth.register.method)

data class GetAvailableChatsResponse(val chats: List<Chat>) : ServerResponse(ApiTransportMapping.Chat.getAvailableChats.method)
data class GetPersonalChatWithResponse(val chat: PersonalChat) : ServerResponse(ApiTransportMapping.Chat.getPersonalChatWith.method)
data class CreateGroupChatResponse(val chat: GroupChat) : ServerResponse(ApiTransportMapping.Chat.createGroupChat.method)

data class GetChatMessagesResponse(val messages: List<Message>) : ServerResponse(ApiTransportMapping.Message.getChatMessages.method)
data class SendTextMessageResponse(val message: TextMessage) : ServerResponse(ApiTransportMapping.Message.sendTextMessage.method)

data class GetUserByUsernameResponse(val user: User?) : ServerResponse(ApiTransportMapping.User.searchByUsername.method)
data class GetUserByIdResponse(val user: User?) : ServerResponse(ApiTransportMapping.User.getUserById.method)

class ServerResponseAdapter: TypeAdapter<ServerResponse> {
    override fun classFor(type: Any): KClass<out ServerResponse> = when (type as String) {
        ApiTransportMapping.Auth.authenticate.method -> AuthenticateResponse::class
        ApiTransportMapping.Auth.register.method -> RegisterResponse::class

        ApiTransportMapping.Chat.getAvailableChats.method -> GetAvailableChatsResponse::class
        ApiTransportMapping.Chat.getPersonalChatWith.method -> GetPersonalChatWithResponse::class
        ApiTransportMapping.Chat.createGroupChat.method -> CreateGroupChatResponse::class

        ApiTransportMapping.Message.getChatMessages.method -> GetChatMessagesResponse::class
        ApiTransportMapping.Message.sendTextMessage.method -> SendTextMessageResponse::class

        ApiTransportMapping.User.searchByUsername.method -> GetUserByUsernameResponse::class
        ApiTransportMapping.User.getUserById.method -> GetUserByIdResponse::class

        else -> throw IllegalArgumentException("Unknown method: $type")
    }
}