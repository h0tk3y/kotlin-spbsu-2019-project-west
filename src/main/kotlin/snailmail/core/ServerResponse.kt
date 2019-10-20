package snailmail.core

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import snailmail.core.api.APIMethodMapping
import snailmail.core.api.AuthenticationResult
import kotlin.reflect.KClass

@TypeFor(field = "method", adapter = ServerResponseAdapter::class)
sealed class ServerResponse(val method: String)

data class AuthenticateResponse(val result: AuthenticationResult) : ServerResponse(APIMethodMapping.Auth.authenticate)
data class RegisterResponse(val result: AuthenticationResult) : ServerResponse(APIMethodMapping.Auth.register)

data class GetAvailableChatsResponse(val chats: List<Chat>) : ServerResponse(APIMethodMapping.Chat.getAvailableChats)
data class GetPersonalChatWithResponse(val chat: PersonalChat) : ServerResponse(APIMethodMapping.Chat.getPersonalChatWith)
data class CreateGroupChatResponse(val chat: GroupChat) : ServerResponse(APIMethodMapping.Chat.createGroupChat)

data class GetChatMessagesResponse(val messages: List<Message>) : ServerResponse(APIMethodMapping.Message.getChatMessages)
data class SendTextMessageResponse(val message: TextMessage) : ServerResponse(APIMethodMapping.Message.sendTextMessage)

data class SearchByUsernameResponse(val user: User?) : ServerResponse(APIMethodMapping.User.searchByUsername)
data class GetUserByIdResponse(val user: User?) : ServerResponse(APIMethodMapping.User.getUserById)

class ServerResponseAdapter: TypeAdapter<ServerResponse> {
    override fun classFor(type: Any): KClass<out ServerResponse> = when (type as String) {
        APIMethodMapping.Auth.authenticate -> AuthenticateResponse::class
        APIMethodMapping.Auth.register -> RegisterResponse::class

        APIMethodMapping.Chat.getAvailableChats -> GetAvailableChatsResponse::class
        APIMethodMapping.Chat.getPersonalChatWith -> GetPersonalChatWithResponse::class
        APIMethodMapping.Chat.createGroupChat -> CreateGroupChatResponse::class

        APIMethodMapping.Message.getChatMessages -> GetChatMessagesResponse::class
        APIMethodMapping.Message.sendTextMessage -> SendTextMessageResponse::class

        APIMethodMapping.User.searchByUsername -> SearchByUsernameResponse::class
        APIMethodMapping.User.getUserById -> GetUserByIdResponse::class

        else -> throw IllegalArgumentException("Unknown method: $type")
    }
}