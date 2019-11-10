package snailmail.server.transport

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.jwt
import io.ktor.auth.principal
import io.ktor.routing.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import snailmail.core.*
import snailmail.core.Api
import snailmail.server.SimpleJwt
import snailmail.server.UserPrincipal
import java.util.*

class HttpServer(private val api: Api, private val secretKey: String) {
    private val simpleJwt = SimpleJwt(secretKey)

    fun run(port: Int = 9999) {
        embeddedServer(Netty, port)
        {
            install(StatusPages) {
                exception<ServerException> { cause ->
                    call.respond(mapOf("error" to cause.errorType()))
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

            install(Authentication) {
                jwt {
                    verifier(simpleJwt.verifier)
                    validate {
                        val userId = UUID.fromString(it.payload.getClaim("id").asString())
                        UserPrincipal(it.toString(), userId)
                    }
                }
            }

            routing {
                post(ApiRouting.authenticate.url) {
                    val req = call.receive<AuthenticateRequest>()
                    val token = api.authenticate(UserCredentials(req.username, req.password))
                    call.respond(AuthenticateResponse(token))
                }

                post(ApiRouting.register.url) {
                    val req = call.receive<RegisterRequest>()
                    val token = api.register(UserCredentials(req.username, req.password))
                    call.respond(RegisterResponse(token))
                }

                authenticate {
                    get(ApiRouting.getChats.url) {
                        val principal = call.principal<UserPrincipal>() ?: error("No principal")
                        call.respond(GetChatsResponse(api.getChats(principal.token)))
                    }

                    get(ApiRouting.getPersonalChatWith.url) {
                        val principal = call.principal<UserPrincipal>() ?: error("No principal")
                        val user = UUID.fromString(call.parameters["user"])
                        call.respond(GetPersonalChatWithResponse(api.getPersonalChatWith(principal.token, user)))
                    }

                    post(ApiRouting.createGroupChat.url) {
                        val principal = call.principal<UserPrincipal>() ?: error("No principal")
                        val req = call.receive<CreateGroupChatRequest>()
                        call.respond(CreateGroupChatResponse(api.createGroupChat(
                                principal.token, req.title, req.invitedMembers
                        )))
                    }

                    get(ApiRouting.getChatMessages.url) {
                        val principal = call.principal<UserPrincipal>() ?: error("No principal")
                        val chat = UUID.fromString(call.parameters["chat"])
                        call.respond(GetChatMessagesResponse(api.getChatMessages(principal.token, chat)))
                    }

                    post(ApiRouting.sendTextMessage.url) {
                        val principal = call.principal<UserPrincipal>() ?: error("No principal")
                        val chat = UUID.fromString(call.parameters["chat"])
                        val text = call.receive<SendTextMessageRequest>().text
                        call.respond(SendTextMessageResponse(api.sendTextMessage(principal.token, text, chat)))
                    }

                    get(ApiRouting.getUserByUsername.url) {
                        val principal = call.principal<UserPrincipal>() ?: error("No principal")
                        val username = call.parameters["username"]!!
                        call.respond(GetUserByUsernameResponse(api.getUserByUsername(principal.token, username)))
                    }

                    get(ApiRouting.getUserById.url) {
                        val principal = call.principal<UserPrincipal>() ?: error("No principal")
                        val id = UUID.fromString(call.parameters["id"])
                        call.respond(GetUserByIdResponse(api.getUserById(principal.token, id)))
                    }
                }
            }
        }.start(wait = true)
    }
}

