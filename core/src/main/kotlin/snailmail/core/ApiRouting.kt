package snailmail.core

object ApiRouting {
    data class Mapping(val method: String, val url: String, val needsToken: Boolean = true)

    val authenticate = Mapping("POST", "/users/authenticate", false)
    val register = Mapping("POST", "/users/register", false)
    val getAvailableChats = Mapping("GET", "/chats")
    val getPersonalChatWith = Mapping("GET", "/chats/personal/{user}")
    val createGroupChat = Mapping("POST", "/chats/group")
    val getChatMessages = Mapping("GET", "/chats/{chat}/messages")
    val sendTextMessage = Mapping("POST", "/chats/{chat}/messages")
    val getUserByUsername = Mapping("GET", "/users/username/{username}")
    val getUserById = Mapping("GET", "/users/id/{username}")
}
