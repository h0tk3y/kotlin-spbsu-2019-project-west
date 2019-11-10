package snailmail.core

import java.util.*

interface Api {
    fun authenticate(credentials: UserCredentials): AuthToken
    fun register(credentials: UserCredentials): AuthToken
    fun getChats(token: AuthToken): List<Chat>
    fun getPersonalChatWith(token: AuthToken, user: UUID): PersonalChat
    fun createGroupChat(token: AuthToken, title: String, invitedMembers: List<UUID>): GroupChat
    fun getChatMessages(token: AuthToken, chat: UUID): List<Message>
    fun sendTextMessage(token: AuthToken, text: String, chat: UUID): TextMessage
    fun getUserByUsername(token: AuthToken, username: String): User?
    fun getUserById(token: AuthToken, id: UUID): User?
}
