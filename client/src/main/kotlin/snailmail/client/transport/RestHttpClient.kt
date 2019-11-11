package snailmail.client.transport

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.post
import kotlinx.coroutines.runBlocking
import snailmail.core.*
import snailmail.core.Api
import snailmail.core.AuthToken
import java.util.*

class RestHttpClient(private val host: String, private val port: Int) : Api {
    override fun changeCredentials(authToken: AuthToken, credentials: UserCredentials): AuthToken {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getChatById(token: AuthToken, chat: UUID): Chat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun joinGroupChatUsingPublicTag(token: AuthToken, tag: String): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun joinGroupChatUsingInviteToken(token: AuthToken, inviteToken: String): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun inviteUserToGroupChat(token: AuthToken, chat: UUID, user: UUID): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun kickUserFromGroupChat(token: AuthToken, chat: UUID, user: UUID): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeUserFromBlacklistOfGroupChat(token: AuthToken, chat: UUID, user: UUID): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun leaveGroupChat(token: AuthToken, chat: UUID): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun changeTitleOfGroupChat(token: AuthToken, chat: UUID, title: String): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateAvatarOfGroupChat(token: AuthToken, chat: UUID, photo: Photo): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updatePublicTagOfGroupChat(token: AuthToken, chat: UUID, publicTag: String): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun withdrawPublicTagOfGroupChat(token: AuthToken, chat: UUID): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun makeNewPrivateInviteToken(token: AuthToken, chat: UUID): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun withdrawPrivateInviteToken(token: AuthToken, chat: UUID): GroupChat {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setPreferredTitleOfGroupChat(token: AuthToken, chat: UUID, title: String): GroupChatPreferences {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGroupChatPreferences(token: AuthToken, chat: UUID): GroupChatPreferences {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun prepareUpload(token: AuthToken, mediaType: String): UUID {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMedia(token: AuthToken, media: UUID): Media {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteMessage(token: AuthToken, message: UUID): DeletedMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun editTextMessage(token: AuthToken, message: UUID, newText: String): TextMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendMediaMessage(token: AuthToken, media: UUID, caption: String): MediaMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun editMediaMessageCaption(token: AuthToken, message: UUID, newCaption: String): MediaMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContact(token: AuthToken, user: UUID): Contact {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateContactDisplayName(token: AuthToken, user: UUID, displayName: String): Contact {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun banContact(token: AuthToken, user: UUID): Contact {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unbanContact(token: AuthToken, user: UUID): Contact {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun checkBannedFor(token: AuthToken, user: UUID): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateProfileDisplayName(token: AuthToken, displayName: String): User {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateProfileEmail(token: AuthToken, email: String?): User {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateProfileAvatar(token: AuthToken, avatar: Photo?): User {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun searchAmongMessages(token: AuthToken, text: String): List<Message> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun searchAmongUsers(token: AuthToken, text: String): List<User> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun searchAmongChats(token: AuthToken, text: String): List<Chat> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val client = HttpClient {
        install(JsonFeature) {
            serializer = JacksonSerializer()
        }
    }

    private val mapper = jacksonObjectMapper()

    private val serverUrl = "http://$host:$port"

    private inline fun <reified T> readResponse(json: String): T {
        println(json)
        try {
            throw mapper.readValue<ServerException>(json)
        } catch (e: JsonMappingException) {
            return mapper.readValue(json)
        }
    }

    override fun authenticate(credentials: UserCredentials): AuthToken = runBlocking {
        val json = client.post<String>("$serverUrl/users/authenticate") {
            body = AuthenticateRequest(credentials.username, credentials.password)
        }
        readResponse<AuthenticateResponse>(json).result
    }

    override fun register(credentials: UserCredentials): AuthToken {
        TODO()
    }

    override fun getChats(token: AuthToken): List<Chat> {
        TODO()
    }

    override fun getPersonalChatWith(token: AuthToken, user: UUID): PersonalChat {
        TODO()
    }

    override fun createGroupChat(token: AuthToken, title: String, invitedMembers: List<UUID>): GroupChat {
        TODO()
    }

    override fun getChatMessages(token: AuthToken, chat: UUID): List<Message> {
        TODO()
    }

    override fun sendTextMessage(token: AuthToken, text: String, chat: UUID): TextMessage {
        TODO()
    }

    override fun getUserByUsername(token: AuthToken, username: String): User {
        TODO()
    }

    override fun getUserById(token: AuthToken, user: UUID): User {
        TODO()
    }
}