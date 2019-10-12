package snailmail.core

import com.beust.klaxon.*
import snailmail.core.api.AuthenticationResult
import kotlin.reflect.KClass
import java.lang.*

@TypeFor(field = "method", adapter = ServerResponseAdapter::class)
sealed class ServerResponse(val method: String)

data class AuthenticateResponse(val result: AuthenticationResult): ServerResponse("auth.authenticate")
data class RegisterResponse(val result: AuthenticationResult): ServerResponse("auth.register")

data class GetAvailableChatsResponse(val chats: List<Chat>): ServerResponse("chat.getAvailableChats")
data class GetPersonalChatWithResponse(val chat: PersonalChat): ServerResponse("chat.getPersonalChatWith")
data class CreateGroupChatResponse(val chat: GroupChat): ServerResponse("chat.createGroupChat")

data class GetChatMessagesResponse(val messages: List<Message>): ServerResponse("message.getChatMessages")
data class SendTextMessageResponse(val message: TextMessage): ServerResponse("message.sendTextMessage")

data class SearchByUsernameResponse(val user: User?): ServerResponse("user.searchByUsername")

class ServerResponseAdapter: TypeAdapter<ServerResponse> {
    override fun classFor(type: Any): KClass<out ServerResponse> = when (type as String) {
        "auth.authenticate" -> AuthenticateResponse::class
        "auth.register" -> RegisterResponse::class
        "chat.getAvailableChats" -> GetAvailableChatsResponse::class
        "chat.getPersonalChatWith" -> GetPersonalChatWithResponse::class
        "chat.createGroupChat" -> CreateGroupChatResponse::class
        "message.getChatMessages" -> GetChatMessagesResponse::class
        "message.sendTextMessage" -> SendTextMessageResponse::class
        "user.searchByUsername" -> SearchByUsernameResponse::class
        else -> throw IllegalArgumentException("Unknown method: $type")
    }
}