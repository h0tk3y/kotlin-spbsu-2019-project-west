package snailmail.core.api

object APITransportMapping {
    data class Mapping(val method: String, val REST: String)

    object Auth {
        val authenticate = Mapping("auth.authenticate", "/auth/{username}/{password}/" /*GET*/)
        val register = Mapping("auth.register", "/auth/{username}/{password}/" /*POST*/)
    }

    object Chat {
        val getAvailableChats = Mapping("chats.getAvailableChats", "/{authToken}/chats/" /*GET*/)
        val getPersonalChatWith = Mapping("chats.getPersonalChatWith", "/{authToken}/chats/personal/{userId}/" /*GET*/)
        val createGroupChat = Mapping("chats.createGroupChat", "/{authToken}/chats/group/{title}/{invitedMembers...}/" /*POST*/)
    }

    object Message {
        val getChatMessages = Mapping("messages.getChatMessages", "/{authToken}/messages/{chatId}/" /*GET*/)
        val sendTextMessage = Mapping("messages.sendTextMessage", "/{authToken}/messages/{chatId}/" /*POST*/)
    }

    object User {
        val searchByUsername = Mapping("users.searchByUsername", "/{authToken}/users/{username}/" /*GET*/)
        val getUserById = Mapping("users.getUserById", "/{authToken}/users/id/{userId}/" /*GET*/)
    }

    object Convention {
        const val method = "method"
        const val errorType = "error-type"
    }
}
