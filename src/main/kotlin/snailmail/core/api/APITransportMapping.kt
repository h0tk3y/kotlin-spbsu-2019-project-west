package snailmail.core.api

object APITransportMapping {
    object Auth {
        const val authenticate = "auth.authenticate"
        const val register = "auth.register"
    }

    object Chat {
        const val getAvailableChats = "chats.getAvailableChats"
        const val getPersonalChatWith = "chats.getPersonalChatWith"
        const val createGroupChat = "chats.createGroupChat"
    }

    object Message {
        const val getChatMessages = "messages.getChatMessages"
        const val sendTextMessage = "messages.sendTextMessage"
    }

    object User {
        const val searchByUsername = "users.searchByUsername"
        const val getUserById = "users.getUserById"
    }

    object Convention {
        const val method = "method"
        const val errorType = "error-type"
    }
}
