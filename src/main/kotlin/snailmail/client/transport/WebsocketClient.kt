package snailmail.client.transport

import com.beust.klaxon.Klaxon
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import snailmail.core.*
import snailmail.core.api.API
import snailmail.core.api.AuthToken
import snailmail.core.api.AuthenticationResult
import java.util.*

class WebsocketClient(private val host: String, private val port: Int) : API {
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

    override fun authenticate(credentials: UserCredentials): AuthenticationResult {
        val res = request(AuthenticateRequest(credentials.username, credentials.password))
        return (res as AuthenticateResponse).result
    }

    override fun register(credentials: UserCredentials): AuthenticationResult {
        val res = request(RegisterRequest(credentials.username, credentials.password))
        return (res as RegisterResponse).result
    }

    override fun getAvailableChats(token: AuthToken): ChatRetriever {
        val res = request(GetAvailableChatsRequest(token))
        return ListChatRetriever((res as GetAvailableChatsResponse).chats)
    }

    override fun getPersonalChatWith(token: AuthToken, user: UUID): PersonalChat {
        val res = request(GetPersonalChatWithRequest(token, user))
        return (res as GetPersonalChatWithResponse).chat
    }

    override fun createGroupChat(token: AuthToken, title: String, invitedMembers: List<UUID>): GroupChat {
        val res = request(CreateGroupChatRequest(token, title, invitedMembers))
        return (res as CreateGroupChatResponse).chat
    }

    override fun getChatMessages(token: AuthToken, chat: UUID): MessageRetriever {
        val res = request(GetChatMessagesRequest(token, chat))
        return ListMessageRetriever((res as GetChatMessagesResponse).messages)
    }

    override fun sendTextMessage(token: AuthToken, text: String, chat: UUID): TextMessage {
        val res = request(SendTextMessageRequest(token, text, chat))
        return (res as SendTextMessageResponse).message
    }

    override fun searchByUsername(token: AuthToken, username: String): User? {
        val res = request(SearchByUsernameRequest(token, username))
        return (res as SearchByUsernameResponse).user
    }
}