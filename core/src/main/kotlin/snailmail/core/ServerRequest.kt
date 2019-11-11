package snailmail.core

import java.util.*

data class AuthenticateRequest(val username: String, val password: String)
data class RegisterRequest(val username: String, val password: String)
data class CreateGroupChatRequest(val title: String, val invitedMembers: List<UUID>)
data class SendTextMessageRequest(val text: String)