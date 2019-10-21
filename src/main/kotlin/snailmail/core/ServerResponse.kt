package snailmail.core

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import snailmail.core.api.APITransportMapping
import snailmail.core.api.AuthenticationResult
import kotlin.reflect.KClass

@TypeFor(field = APITransportMapping.Convention.method, adapter = ServerResponseAdapter::class)
sealed class ServerResponse(val method: String)

data class AuthenticateResponse(val result: AuthenticationResult) : ServerResponse(APITransportMapping.Auth.authenticate)
data class RegisterResponse(val result: AuthenticationResult) : ServerResponse(APITransportMapping.Auth.register)

data class GetAvailableChatsResponse(val chats: List<Chat>) : ServerResponse(APITransportMapping.Chat.getAvailableChats)
data class GetPersonalChatWithResponse(val chat: PersonalChat) : ServerResponse(APITransportMapping.Chat.getPersonalChatWith)
data class CreateGroupChatResponse(val chat: GroupChat) : ServerResponse(APITransportMapping.Chat.createGroupChat)

data class GetChatMessagesResponse(val messages: List<Message>) : ServerResponse(APITransportMapping.Message.getChatMessages)
data class SendTextMessageResponse(val message: TextMessage) : ServerResponse(APITransportMapping.Message.sendTextMessage)

data class SearchByUsernameResponse(val user: User?) : ServerResponse(APITransportMapping.User.searchByUsername)
data class GetUserByIdResponse(val user: User?) : ServerResponse(APITransportMapping.User.getUserById)

class ServerResponseAdapter: TypeAdapter<ServerResponse> {
    override fun classFor(type: Any): KClass<out ServerResponse> = when (type as String) {
        APITransportMapping.Auth.authenticate -> AuthenticateResponse::class
        APITransportMapping.Auth.register -> RegisterResponse::class

        APITransportMapping.Chat.getAvailableChats -> GetAvailableChatsResponse::class
        APITransportMapping.Chat.getPersonalChatWith -> GetPersonalChatWithResponse::class
        APITransportMapping.Chat.createGroupChat -> CreateGroupChatResponse::class

        APITransportMapping.Message.getChatMessages -> GetChatMessagesResponse::class
        APITransportMapping.Message.sendTextMessage -> SendTextMessageResponse::class

        APITransportMapping.User.searchByUsername -> SearchByUsernameResponse::class
        APITransportMapping.User.getUserById -> GetUserByIdResponse::class

        else -> throw IllegalArgumentException("Unknown method: $type")
    }
}