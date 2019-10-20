package snailmail.core

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import snailmail.core.api.APIMethodMapping
import snailmail.core.api.AuthToken
import java.util.*
import kotlin.reflect.KClass

@TypeFor(field = "method", adapter = ServerRequestAdapter::class)
sealed class ServerRequest(val method: String)

data class AuthenticateRequest(val username: String, val password: String) : ServerRequest(APIMethodMapping.Auth.authenticate)
data class RegisterRequest(val username: String, val password: String) : ServerRequest(APIMethodMapping.Auth.register)

data class GetAvailableChatsRequest(val token: AuthToken) : ServerRequest(APIMethodMapping.Chat.getAvailableChats)
data class GetPersonalChatWithRequest(val token: AuthToken, val user: UUID) : ServerRequest(APIMethodMapping.Chat.getPersonalChatWith)
data class CreateGroupChatRequest(val token: AuthToken, val title: String,
                                  val invitedMembers: List<UUID>) : ServerRequest(APIMethodMapping.Chat.createGroupChat)

data class GetChatMessagesRequest(val token: AuthToken, val chat: UUID) : ServerRequest(APIMethodMapping.Message.getChatMessages)
data class SendTextMessageRequest(val token: AuthToken, val text: String,
                                  val chat: UUID) : ServerRequest(APIMethodMapping.Message.sendTextMessage)

data class SearchByUsernameRequest(val token: AuthToken, val username: String) : ServerRequest(APIMethodMapping.User.searchByUsername)
data class GetUserByIdRequest(val token: AuthToken, val id: UUID) : ServerRequest(APIMethodMapping.User.getUserById)

class ServerRequestAdapter : TypeAdapter<ServerRequest> {
    override fun classFor(type: Any): KClass<out ServerRequest> = when (type as String) {
        APIMethodMapping.Auth.authenticate -> AuthenticateRequest::class
        APIMethodMapping.Auth.register -> RegisterRequest::class

        APIMethodMapping.Chat.getAvailableChats -> GetAvailableChatsRequest::class
        APIMethodMapping.Chat.getPersonalChatWith -> GetPersonalChatWithRequest::class
        APIMethodMapping.Chat.createGroupChat -> CreateGroupChatRequest::class

        APIMethodMapping.Message.getChatMessages -> GetChatMessagesRequest::class
        APIMethodMapping.Message.sendTextMessage -> SendTextMessageRequest::class

        APIMethodMapping.User.searchByUsername -> SearchByUsernameRequest::class
        APIMethodMapping.User.getUserById -> GetUserByIdRequest::class

        else -> throw IllegalArgumentException("Unknown method: $type")
    }
}