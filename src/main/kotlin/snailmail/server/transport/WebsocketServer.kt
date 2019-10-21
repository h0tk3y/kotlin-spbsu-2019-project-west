package snailmail.server.transport

import com.beust.klaxon.Klaxon
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
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
import snailmail.core.api.API
import snailmail.core.api.APITransportMapping
import java.util.*

class WebsocketServer(private val api: API) {
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
        is GetUserByIdRequest ->
            GetUserByIdResponse(api.getUserById(req.token, req.id))
    }

    fun run(port: Int = 9999) {
        fun methodToURIPath(method: String) = "/api/" + method.replace('.', '/')

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
                get(methodToURIPath(APITransportMapping.Auth.authenticate)) {
                    safeProcessing {
                        val username = call.request.queryParameters["username"]
                                ?: throw ProtocolErrorException()
                        val password = call.request.queryParameters["password"]
                                ?: throw ProtocolErrorException()
                        handleHTTPRequest(AuthenticateRequest(username, password))
                    }
                }
                post(methodToURIPath(APITransportMapping.Auth.register)) {
                    safeProcessing {
                        val username = call.request.queryParameters["username"]
                                ?: throw ProtocolErrorException()
                        val password = call.request.queryParameters["password"]
                                ?: throw ProtocolErrorException()
                        handleHTTPRequest(RegisterRequest(username, password))
                    }
                }

                get(methodToURIPath(APITransportMapping.Chat.getAvailableChats)) {
                    authenticated { token ->
                        handleHTTPRequest(GetAvailableChatsRequest(token))
                    }
                }
                get(methodToURIPath(APITransportMapping.Chat.getPersonalChatWith)) {
                    authenticated { token ->
                        val user = UUID.fromString(call.request.queryParameters["user"]
                                ?: throw ProtocolErrorException())
                        handleHTTPRequest(GetPersonalChatWithRequest(token, user))
                    }
                }
                post(methodToURIPath(APITransportMapping.Chat.createGroupChat)) {
                    authenticated { token ->
                        val title = call.request.queryParameters["title"] ?: throw ProtocolErrorException()
                        val invitedMembers = call.request.queryParameters.getAll("invite")?.map { UUID.fromString(it) }
                                ?: listOf()
                        handleHTTPRequest(CreateGroupChatRequest(token, title, invitedMembers))
                    }
                }

                get(methodToURIPath(APITransportMapping.Message.getChatMessages)) {
                    authenticated { token ->
                        val chat = UUID.fromString(call.request.queryParameters["chat"]
                                ?: throw ProtocolErrorException())
                        handleHTTPRequest(GetChatMessagesRequest(token, chat))
                    }
                }
                post(methodToURIPath(APITransportMapping.Message.sendTextMessage)) {
                    authenticated { token ->
                        val chat = UUID.fromString(call.request.queryParameters["chat"]
                                ?: throw ProtocolErrorException())
                        val text = call.request.queryParameters["text"] ?: throw ProtocolErrorException()
                        handleHTTPRequest(SendTextMessageRequest(token, text, chat))
                    }
                }

                get(methodToURIPath(APITransportMapping.User.getUserById)) {
                    authenticated { token ->
                        val id = UUID.fromString(call.request.queryParameters["id"] ?: throw ProtocolErrorException())
                        handleHTTPRequest(GetUserByIdRequest(token, id))
                    }
                }
                get(methodToURIPath(APITransportMapping.User.searchByUsername)) {
                    authenticated { token ->
                        val username = call.request.queryParameters["username"] ?: throw ProtocolErrorException()
                        handleHTTPRequest(SearchByUsernameRequest(token, username))
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
        val token = call.request.queryParameters["token"] ?: throw InvalidTokenException()
        block(token)
    }

    private suspend fun <T> PipelineContext<Unit, ApplicationCall>.handleWithCode(code: HttpStatusCode, e: T) {
        val res = klaxon.toJsonString(e)
        call.respondText(res, status = code, contentType = ContentType.Application.Json)
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.handleHTTPRequest(req: ServerRequest) {
        try {
            handleWithCode(HttpStatusCode.OK, klaxon.toJsonString(processRequest(req)))
        } catch (e: InvalidTokenException) {
            handleWithCode(HttpStatusCode.Unauthorized, e)
        } catch (e: InvalidChatId) {
            handleWithCode(HttpStatusCode.NotFound, e)
        } catch (e: UserIsNotMemberException) {
            handleWithCode(HttpStatusCode.Forbidden, e)
        }
    }
}

