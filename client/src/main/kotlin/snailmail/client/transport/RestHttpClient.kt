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

    override fun getUserByUsername(token: AuthToken, username: String): User? {
        TODO()
    }

    override fun getUserById(token: AuthToken, id: UUID): User? {
        TODO()
    }
}