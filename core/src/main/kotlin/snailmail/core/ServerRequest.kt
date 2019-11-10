package snailmail.core

import java.util.*

data class AuthenticateRequest(val username: String, val password: String)
data class RegisterRequest(val username: String, val password: String)
data class GetAvailableChatsRequest(val token: AuthToken)
data class GetPersonalChatWithRequest(val token: AuthToken, val user: UUID)
data class CreateGroupChatRequest(val token: AuthToken, val title: String, val invitedMembers: List<UUID>)
data class GetChatMessagesRequest(val token: AuthToken, val chat: UUID)
data class SendTextMessageRequest(val token: AuthToken, val text: String, val chat: UUID)
data class GetUserByUsernameRequest(val token: AuthToken, val username: String)
data class GetUserByIdRequest(val token: AuthToken, val id: UUID)