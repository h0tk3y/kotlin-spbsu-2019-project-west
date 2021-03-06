package snailmail.server.transport

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.jwt
import io.ktor.auth.parseAuthorizationHeader
import io.ktor.auth.principal
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.event.Level
import snailmail.core.*
import snailmail.server.SimpleJwt
import snailmail.server.UserPrincipal
import java.util.*

class RestHttpServer(private val api: Api, private val secretKey: String) {
    private val simpleJwt = SimpleJwt(secretKey)

    fun run(port: Int = 9999) {
        embeddedServer(Netty, port, module = restServer()).start(wait = true)
    }

    fun restServer() = fun Application.() {
        install(StatusPages) {
            exception<ServerException> { cause ->
                call.respond(cause)
            }
            exception<Throwable> { cause ->
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
                throw cause
            }
        }

        install(ContentNegotiation) {
            jackson {
                enable(SerializationFeature.INDENT_OUTPUT)
            }
        }

        install(CallLogging) {
            level = Level.INFO
        }

        install(Authentication) {
            jwt {
                verifier(simpleJwt.verifier)
                validate {
                    val userId = UUID.fromString(it.payload.getClaim("id").asString())
                    UserPrincipal(this.request.parseAuthorizationHeader()?.render()!!.removePrefix("Bearer "), userId)
                }
            }
        }

        routing {
            post("/users/authenticate") {
                val req: AuthenticateRequest
                try {
                    req = call.receive<AuthenticateRequest>()
                } catch (e: Exception) {
                    call.respond(ProtocolErrorException())
                    return@post
                }
                val token = api.authenticate(UserCredentials(req.username, req.password))
                call.respond(AuthenticateResponse(token))
            }

            post("/users/register") {
                val req: RegisterRequest
                try {
                    req = call.receive<RegisterRequest>()
                } catch (e: Exception) {
                    call.respond(ProtocolErrorException())
                    return@post
                }
                val token = api.register(UserCredentials(req.username, req.password))
                call.respond(RegisterResponse(token))
            }

            authenticate {
                get("/chats") {
                    val principal = call.principal<UserPrincipal>() ?: error("No principal")
                    call.respond(GetChatsResponse(api.getChats(principal.token)))
                }

                get("/chats/personal/{user}") {
                    val principal = call.principal<UserPrincipal>() ?: error("No principal")
                    val user = UUID.fromString(call.parameters["user"])
                    call.respond(GetPersonalChatWithResponse(api.getPersonalChatWith(principal.token, user)))
                }

                post("/chats/group") {
                    val principal = call.principal<UserPrincipal>() ?: error("No principal")
                    val req = call.receive<CreateGroupChatRequest>()
                    call.respond(CreateGroupChatResponse(api.createGroupChat(
                            principal.token, req.title, req.invitedMembers
                    )))
                }

                get("/chats/{chat}/messages") {
                    val principal = call.principal<UserPrincipal>() ?: error("No principal")
                    val chat = UUID.fromString(call.parameters["chat"])
                    call.respond(GetChatMessagesResponse(api.getChatMessages(principal.token, chat)))
                }

                post("/chats/{chat}/messages") {
                    val principal = call.principal<UserPrincipal>() ?: error("No principal")
                    val chat = UUID.fromString(call.parameters["chat"])
                    val text = call.receive<SendTextMessageRequest>().text
                    call.respond(SendTextMessageResponse(api.sendTextMessage(principal.token, text, chat)))
                }

                get("/users/username/{username}") {
                    val principal = call.principal<UserPrincipal>() ?: error("No principal")
                    val username = call.parameters["username"]!!
                    call.respond(GetUserByUsernameResponse(api.getUserByUsername(principal.token, username)))
                }

                get("/users/id/{id}") {
                    val principal = call.principal<UserPrincipal>() ?: error("No principal")
                    val id = UUID.fromString(call.parameters["id"])
                    call.respond(GetUserByIdResponse(api.getUserById(principal.token, id)))
                }
            }
        }
    }
}

