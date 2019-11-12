package snailmail.server.transport

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import snailmail.core.*
import snailmail.server.Server
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class RestHttpServerTest {
    private val mapper = jacksonObjectMapper()

    @Test
    fun `sample good registration`() = runServer { _ ->
        registerRequest("user", "pass") {
            assertEquals(HttpStatusCode.OK, response.status())
            val result = mapper.readValue<RegisterResponse>(response.content!!)
            assert(result.result.isNotEmpty())
        }
    }

    @Test
    fun `registration must reject empty password`() = runServer { _ ->
        registerRequest("goodUsername", "") {
            assertEquals(HttpStatusCode.OK, response.status())
            val result = mapper.readValue<ProtocolErrorException>(response.content!!)
            assertNotEquals("", result.error)
        }
    }

    @Test
    fun `registration must reject empty username`() = runServer { _ ->
        registerRequest("", "good password") {
            assertEquals(HttpStatusCode.OK, response.status())
            val result = mapper.readValue<ProtocolErrorException>(response.content!!)
            assertNotEquals("", result.error)
        }
    }

    @Test
    fun `server must reject registration if username is already taken`() = runServer { _ ->
        registerRequest("user", "12345") {
            assertEquals(HttpStatusCode.OK, response.status())
        }
        registerRequest("user", "23456789") {
            assertEquals(HttpStatusCode.OK, response.status())
            val result = mapper.readValue<UnavailableUsernameException>(response.content!!)
            assertNotEquals("", result.error)
        }
    }

    @Test
    fun `sample good authentication`() = runServer { server ->
        server.register(UserCredentials("user", "12345"))
        authenticateRequest("user", "12345") {
            assertEquals(HttpStatusCode.OK, response.status())
            val result = mapper.readValue<AuthenticateResponse>(response.content!!)
            assert(result.result.isNotEmpty())
        }
    }

    private fun runServer(block: TestApplicationEngine.(Server) -> Unit) {
        val sampleSecret = "secret"
        val server = Server(sampleSecret)
        withTestApplication(RestHttpServer(server, sampleSecret).restServer(), test = { block(server) })
    }

    private fun TestApplicationEngine.registerRequest(username: String, password: String, block: TestApplicationCall.() -> Unit) =
            with(handleRequest(HttpMethod.Post, "/users/register") {
                addHeader("Content-Type", "application/json")
                setBody("""{"username":"$username", "password": "$password"}""")
            }, block)

    private fun TestApplicationEngine.authenticateRequest(username: String, password: String, block: TestApplicationCall.() -> Unit) =
            with(handleRequest(HttpMethod.Post, "/users/authenticate") {
                addHeader("Content-Type", "application/json")
                setBody("""{"username":"$username", "password": "$password"}""")
            }, block)
}