package snailmail.core

import com.beust.klaxon.*
import snailmail.core.api.AuthToken
import java.lang.*
import java.util.*
import kotlin.reflect.KClass

@TypeFor(field = "method", adapter = ServerRequestAdapter::class)
sealed class ServerRequest(val method: String)

data class AuthenticateRequest(val username: String, val password: String): ServerRequest("auth.authenticate")
data class RegisterRequest(val username: String, val password: String): ServerRequest("auth.register")

data class GetAvailableChatsRequest(val token: AuthToken): ServerRequest("chat.getAvailableChats")
data class GetPersonalChatWithRequest(val token: AuthToken, val user: UUID): ServerRequest("chat.getPersonalChatWith")
data class CreateGroupChatRequest(val token: AuthToken, val title: String,
                                  val invitedMembers: List<UUID>): ServerRequest("chat.createGroupChat")

data class GetChatMessagesRequest(val token: AuthToken, val chat: UUID): ServerRequest("message.getChatMessages")
data class SendTextMessageRequest(val token: AuthToken, val text: String,
                                  val chat: UUID): ServerRequest("message.sendTextMessage")

data class SearchByUsernameRequest(val token: AuthToken, val username: String): ServerRequest("user.searchByUsername")


class ServerRequestAdapter : TypeAdapter<ServerRequest> {
    override fun classFor(type: Any): KClass<out ServerRequest> = when (type as String) {
        "auth.authenticate" -> AuthenticateRequest::class
        "auth.register" -> RegisterRequest::class
        "chat.getAvailableChats" -> GetAvailableChatsRequest::class
        "chat.getPersonalChatWith" -> GetPersonalChatWithRequest::class
        "chat.createGroupChat" -> CreateGroupChatRequest::class
        "message.getChatMessages" -> GetChatMessagesRequest::class
        "message.sendTextMessage" -> SendTextMessageRequest::class
        "user.searchByUsername" -> SearchByUsernameRequest::class
        else -> throw IllegalArgumentException("Unknown method: $type")
    }
}