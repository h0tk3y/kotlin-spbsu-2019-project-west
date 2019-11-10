package snailmail.server.transport

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.features.BadRequestException
import io.ktor.routing.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import snailmail.core.*
import snailmail.core.Api

class HttpServer(private val api: Api) {
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
            }
        }.start(wait = true)
    }
}

