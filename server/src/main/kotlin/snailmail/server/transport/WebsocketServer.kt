package snailmail.server.transport

import com.beust.klaxon.Klaxon
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.pipeline.PipelineContext
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import snailmail.core.*
import snailmail.core.api.Api
import snailmail.core.api.ApiTransportMapping
import java.util.*

class WebsocketServer(private val api: Api) {
    private val klaxon = Klaxon()
            .converter(UUIDConverter())
            .converter(DateConverter())
            .converter(ServerExceptionConverter())

    private fun processRequest(req: ServerRequest): ServerResponse = when (req) {
        is AuthenticateRequest ->
            AuthenticateResponse(api.authenticate(UserCredentials(req.username, req.password)))
        is RegisterRequest ->
            RegisterResponse(api.register(UserCredentials(req.username, req.password)))
        is GetAvailableChatsRequest ->
            GetAvailableChatsResponse(api.getAvailableChats(req.token))
        is GetPersonalChatWithRequest ->
            GetPersonalChatWithResponse(api.getPersonalChatWith(req.token, req.user))
        is CreateGroupChatRequest ->
            CreateGroupChatResponse(api.createGroupChat(req.token, req.title, req.invitedMembers))
        is GetChatMessagesRequest ->
            GetChatMessagesResponse(api.getChatMessages(req.token, req.chat))
        is SendTextMessageRequest ->
            SendTextMessageResponse(api.sendTextMessage(req.token, req.text, req.chat))
        is GetUserByUsernameRequest ->
            GetUserByUsernameResponse(api.getUserByUsername(req.token, req.username))
        is GetUserByIdRequest ->
            GetUserByIdResponse(api.getUserById(req.token, req.id))
    }

    fun run(port: Int = 9999) {
        fun restPath(path: String) = "/api/" + path.trimStart('/')

        embeddedServer(Netty, port)
        {
            install(WebSockets)
            routing {
                webSocket("/") {
                    while (true) {
                        try {
                            val frame = (incoming.receive() as? Frame.Text)
                                    ?: throw ProtocolErrorException()
                            val text = frame.readText()
                            println(text)
                            val req = klaxon.parse<ServerRequest>(text)
                                    ?: throw ProtocolErrorException()
                            val res = klaxon.toJsonString(processRequest(req))
                            outgoing.send(Frame.Text(res))
                        } catch (e: ServerException) {
                            println("Server Exception: ${e.message}")
                            val res = klaxon.toJsonString(e)
                            outgoing.send(Frame.Text(res))
                        } catch (e: Throwable) {
                            println(e.message)
                            e.stackTrace.forEach { println(it) }
                            val error = ProtocolErrorException()
                            val res = klaxon.toJsonString(error)
                            outgoing.send(Frame.Text(res))
                        }
                    }
                }

                // HTTP REST API routes

                get(restPath(ApiTransportMapping.Auth.authenticate.Rest)) {
                    safeProcessing {
                        val username = call.parameters["username"]
                                ?: throw ProtocolErrorException()
                        val password = call.parameters["password"]
                                ?: throw ProtocolErrorException()
                        handleHttpRequest(AuthenticateRequest(username, password))
                    }
                }
                post(restPath(ApiTransportMapping.Auth.register.Rest)) {
                    safeProcessing {
                        val username = call.parameters["username"]
                                ?: throw ProtocolErrorException()
                        val password = call.parameters["password"]
                                ?: throw ProtocolErrorException()
                        handleHttpRequest(RegisterRequest(username, password))
                    }
                }

                get(restPath(ApiTransportMapping.Chat.getAvailableChats.Rest)) {
                    authenticated { token ->
                        handleHttpRequest(GetAvailableChatsRequest(token))
                    }
                }
                get(restPath(ApiTransportMapping.Chat.getPersonalChatWith.Rest)) {
                    authenticated { token ->
                        val userId = UUID.fromString(call.parameters["userId"]
                                ?: throw ProtocolErrorException())
                        handleHttpRequest(GetPersonalChatWithRequest(token, userId))
                    }
                }
                post(restPath(ApiTransportMapping.Chat.createGroupChat.Rest)) {
                    authenticated { token ->
                        val title = call.parameters["title"] ?: throw ProtocolErrorException()
                        val invitedMembers = call.parameters.getAll("invitedMembers")?.map { UUID.fromString(it) }
                                ?: listOf()
                        handleHttpRequest(CreateGroupChatRequest(token, title, invitedMembers))
                    }
                }

                get(restPath(ApiTransportMapping.Message.getChatMessages.Rest)) {
                    authenticated { token ->
                        val chat = UUID.fromString(call.parameters["chatId"]
                                ?: throw ProtocolErrorException())
                        handleHttpRequest(GetChatMessagesRequest(token, chat))
                    }
                }
                post(restPath(ApiTransportMapping.Message.sendTextMessage.Rest)) {
                    authenticated { token ->
                        val chat = UUID.fromString(call.parameters["chatId"]
                                ?: throw ProtocolErrorException())
                        val text = call.receiveText()
                        handleHttpRequest(SendTextMessageRequest(token, text, chat))
                    }
                }

                get(restPath(ApiTransportMapping.User.getUserById.Rest)) {
                    authenticated { token ->
                        val id = UUID.fromString(call.parameters["userId"] ?: throw ProtocolErrorException())
                        handleHttpRequest(GetUserByIdRequest(token, id))
                    }
                }
                get(restPath(ApiTransportMapping.User.searchByUsername.Rest)) {
                    authenticated { token ->
                        val username = call.parameters["username"] ?: throw ProtocolErrorException()
                        handleHttpRequest(GetUserByUsernameRequest(token, username))
                    }
                }
            }
        }.start(wait = true)
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.safeProcessing(block: suspend PipelineContext<Unit,ApplicationCall>.() -> Unit) {
        try {
            block()
        } catch (e: ProtocolErrorException) {
            val error = ProtocolErrorException()
            val res = klaxon.toJsonString(error)
            this.call.respondText(res, status = HttpStatusCode.BadRequest, contentType = ContentType.Application.Json)
        } catch (e: Throwable) {
            val error = InternalServerErrorException("Something bad happened...")
            val res = klaxon.toJsonString(error)
            call.respondText(res, status = HttpStatusCode.InternalServerError, contentType = ContentType.Application.Json)
        }
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.authenticated(block: suspend PipelineContext<Unit,ApplicationCall>.(token: String) -> Unit) = safeProcessing {
        val token = call.parameters["authToken"] ?: throw InvalidTokenException()
        block(token)
    }

    private suspend fun <T> PipelineContext<Unit, ApplicationCall>.handleWithCode(code: HttpStatusCode, e: T) {
        val res = klaxon.toJsonString(e)
        call.respondText(res, status = code, contentType = ContentType.Application.Json)
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.handleHttpRequest(req: ServerRequest) {
        try {
            handleWithCode(HttpStatusCode.OK, klaxon.toJsonString(processRequest(req)))
        } catch (e: InvalidTokenException) {
            handleWithCode(HttpStatusCode.Unauthorized, e)
        } catch (e: InvalidChatIdException) {
            handleWithCode(HttpStatusCode.NotFound, e)
        } catch (e: UserIsNotMemberException) {
            handleWithCode(HttpStatusCode.Forbidden, e)
        }
    }
}

