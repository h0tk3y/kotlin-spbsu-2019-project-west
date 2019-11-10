package snailmail.core


data class AuthenticateResponse(val result: AuthToken)
data class RegisterResponse(val result: AuthToken)
data class GetAvailableChatsResponse(val chats: List<Chat>)
data class GetPersonalChatWithResponse(val chat: PersonalChat)
data class CreateGroupChatResponse(val chat: GroupChat)
data class GetChatMessagesResponse(val messages: List<Message>)
data class SendTextMessageResponse(val message: TextMessage)
data class GetUserByUsernameResponse(val user: User?)
data class GetUserByIdResponse(val user: User?)