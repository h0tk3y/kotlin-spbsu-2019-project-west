package snailmail.client.transport

import com.beust.klaxon.Klaxon
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import snailmail.core.*
import snailmail.core.Api
import snailmail.core.AuthToken
import java.util.*

class WebsocketClient(private val host: String, private val port: Int) : Api {
    private val client = HttpClient {
        install(WebSockets)
    }

    private val requests = Channel<ServerRequest>()
    private val responses = Channel<ServerResponse>()

    suspend fun run() {
        val klaxon = Klaxon()
                .converter(UUIDConverter())
                .converter(DateConverter())

        client.ws(
                method = HttpMethod.Get,
                host = host, port = port,
                path = "/"
        ) {
            while (true) {
                val req = requests.receive()
                outgoing.send(Frame.Text(klaxon.toJsonString(req)))
                val frame = incoming.receive()
                if (frame is Frame.Text) {
                    val response = klaxon.parse<ServerResponse>(frame.readText())
                    responses.send(response!!)
                }
            }
        }
    }

    private fun request(req: ServerRequest): ServerResponse {
        return runBlocking {
            requests.send(req)
            responses.receive()
        }
    }

    override fun authenticate(credentials: UserCredentials): AuthToken {
        val res = request(AuthenticateRequest(credentials.username, credentials.passwordHash))
        return (res as AuthenticateResponse).result
    }

    override fun register(credentials: UserCredentials): AuthToken {
        val res = request(RegisterRequest(credentials.username, credentials.passwordHash))
        return (res as RegisterResponse).result
    }

    override fun getChats(token: AuthToken): List<Chat> {
        val res = request(GetChatsRequest(token))
        return (res as GetChatsResponse).chats
    }

    override fun getPersonalChatWith(token: AuthToken, user: UUID): PersonalChat {
        val res = request(GetPersonalChatWithRequest(token, user))
        return (res as GetPersonalChatWithResponse).chat
    }

    override fun createGroupChat(token: AuthToken, title: String, invitedMembers: List<UUID>): GroupChat {
        val res = request(CreateGroupChatRequest(token, title, invitedMembers))
        return (res as CreateGroupChatResponse).chat
    }

    override fun getChatMessages(token: AuthToken, chat: UUID): List<Message> {
        val res = request(GetChatMessagesRequest(token, chat))
        return (res as GetChatMessagesResponse).messages
    }

    override fun sendTextMessage(token: AuthToken, text: String, chat: UUID): TextMessage {
        val res = request(SendTextMessageRequest(token, text, chat))
        return (res as SendTextMessageResponse).message
    }

    override fun getUserByUsername(token: AuthToken, username: String): User? {
        val res = request(GetUserByUsernameRequest(token, username))
        return (res as GetUserByUsernameResponse).user
    }

    override fun getUserById(token: AuthToken, id: UUID): User? {
        val res = request(GetUserByIdRequest(token, id))
        return (res as GetUserByIdResponse).user
    }
}