package snailmail.core

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import snailmail.core.api.APITransportMapping
import snailmail.core.api.AuthToken
import kotlin.reflect.KClass

@TypeFor(field = APITransportMapping.Convention.method, adapter = ServerResponseAdapter::class)
sealed class ServerResponse(val method: String)


data class AuthenticateResponse(val result: AuthToken) : ServerResponse(APITransportMapping.Auth.authenticate.method)
data class RegisterResponse(val result: AuthToken) : ServerResponse(APITransportMapping.Auth.register.method)

data class GetAvailableChatsResponse(val chats: List<Chat>) : ServerResponse(APITransportMapping.Chat.getAvailableChats.method)
data class GetPersonalChatWithResponse(val chat: PersonalChat) : ServerResponse(APITransportMapping.Chat.getPersonalChatWith.method)
data class CreateGroupChatResponse(val chat: GroupChat) : ServerResponse(APITransportMapping.Chat.createGroupChat.method)

data class GetChatMessagesResponse(val messages: List<Message>) : ServerResponse(APITransportMapping.Message.getChatMessages.method)
data class SendTextMessageResponse(val message: TextMessage) : ServerResponse(APITransportMapping.Message.sendTextMessage.method)

data class SearchByUsernameResponse(val user: User?) : ServerResponse(APITransportMapping.User.searchByUsername.method)
data class GetUserByIdResponse(val user: User?) : ServerResponse(APITransportMapping.User.getUserById.method)

class ServerResponseAdapter: TypeAdapter<ServerResponse> {
    override fun classFor(type: Any): KClass<out ServerResponse> = when (type as String) {
        APITransportMapping.Auth.authenticate.method -> AuthenticateResponse::class
        APITransportMapping.Auth.register.method -> RegisterResponse::class

        APITransportMapping.Chat.getAvailableChats.method -> GetAvailableChatsResponse::class
        APITransportMapping.Chat.getPersonalChatWith.method -> GetPersonalChatWithResponse::class
        APITransportMapping.Chat.createGroupChat.method -> CreateGroupChatResponse::class

        APITransportMapping.Message.getChatMessages.method -> GetChatMessagesResponse::class
        APITransportMapping.Message.sendTextMessage.method -> SendTextMessageResponse::class

        APITransportMapping.User.searchByUsername.method -> SearchByUsernameResponse::class
        APITransportMapping.User.getUserById.method -> GetUserByIdResponse::class

        else -> throw IllegalArgumentException("Unknown method: $type")
    }
}