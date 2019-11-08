package snailmail.core.api

import snailmail.core.*
import java.util.*

interface Api {
    @Endpoint("/users/authenticate", "POST")
    fun authenticate(credentials: UserCredentials): AuthToken

    @Endpoint("/users/register", "POST")
    fun register(credentials: UserCredentials): AuthToken

    @Endpoint("/chats", "GET")
    fun getAvailableChats(token: AuthToken): List<Chat>

    @Endpoint("/chats/personal/{user}", "GET")
    fun getPersonalChatWith(token: AuthToken, user: UUID): PersonalChat

    @Endpoint("/chats/group", "POST")
    fun createGroupChat(token: AuthToken, title: String, invitedMembers: List<UUID>): GroupChat

    @Endpoint("/chats/{chat}/messages", "GET")
    fun getChatMessages(token: AuthToken, chat: UUID): List<Message>

    @Endpoint("/chats/{chat}/messages", "POST")
    fun sendTextMessage(token: AuthToken, text: String, chat: UUID): TextMessage

    @Endpoint("/users/username/{username}", "GET")
    fun getUserByUsername(token: AuthToken, username: String): User?

    @Endpoint("/users/id/{id}", "GET")
    fun getUserById(token: AuthToken, id: UUID): User?
}
