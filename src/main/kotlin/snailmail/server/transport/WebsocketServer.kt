package snailmail.server.transport

import com.beust.klaxon.Klaxon
import io.ktor.application.install
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import snailmail.core.*
import snailmail.core.api.API

class WebsocketServer(private val api : API) {
    private fun processRequest(req: ServerRequest): ServerResponse = when (req) {
        is AuthenticateRequest ->
            AuthenticateResponse(api.authenticate(UserCredentials(req.username, req.password)))

        is RegisterRequest ->
            RegisterResponse(api.register(UserCredentials(req.username, req.password)))

        is GetAvailableChatsRequest ->
            GetAvailableChatsResponse(api.getAvailableChats(req.token).getChats())

        is GetPersonalChatWithRequest ->
            GetPersonalChatWithResponse(api.getPersonalChatWith(req.token, req.user))

        is CreateGroupChatRequest ->
            CreateGroupChatResponse(api.createGroupChat(req.token, req.title, req.invitedMembers))

        is GetChatMessagesRequest ->
            GetChatMessagesResponse(api.getChatMessages(req.token, req.chat).getMessages())

        is SendTextMessageRequest ->
            SendTextMessageResponse(api.sendTextMessage(req.token, req.text, req.chat))

        is SearchByUsernameRequest ->
            SearchByUsernameResponse(api.searchByUsername(req.token, req.username))
    }

    fun run(port: Int = 9999) {
        val klaxon = Klaxon().converter(UUIDConverter())

        embeddedServer(Netty, port) {
            install(WebSockets)
            routing {
                webSocket("/") {
                    while (true) {
                        val frame = incoming.receive()
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            val req = klaxon.parse<ServerRequest>(text)
                            if (req != null) {
                                val res = klaxon.toJsonString(processRequest(req))
                                outgoing.send(Frame.Text(res))
                            }
                        }
                    }
                }
            }
        }.start(wait = true)
    }
}