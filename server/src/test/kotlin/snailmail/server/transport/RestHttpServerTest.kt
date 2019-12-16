package snailmail.server.transport

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import snailmail.core.*
import snailmail.server.Server
import snailmail.server.data.MySQL
import kotlin.test.assertEquals

internal class RestHttpServerTest {
    private val mapper = jacksonObjectMapper()

    @BeforeEach
    fun truncateTables() {
        MySQL.deleteDB()
    }

    private fun connection(): MySQL {
        val url = "jdbc:h2:mem:test;DATABASE_TO_UPPER=false"
        Database.connect(url, driver = "org.h2.Driver")
        return MySQL()
    }

    @Nested
    inner class `register method` {
        @Test
        fun `sample good registration`() = runServer { _ ->
            registerRequest("user", "pass") {
                assertEquals(HttpStatusCode.OK, response.status())
                val result = mapper.readValue<RegisterResponse>(response.content!!)
                assert(result.result.isNotEmpty())
            }
        }

        @Test
        fun `must reject empty username & password`() = runServer { _ ->
            registerRequest("goodUsername", "") {
                assertEquals(HttpStatusCode.OK, response.status())
                val result = mapper.readValue<ProtocolErrorException>(response.content!!)
                assertNotEquals("", result.error)
            }
            registerRequest("", "good password") {
                assertEquals(HttpStatusCode.OK, response.status())
                val result = mapper.readValue<ProtocolErrorException>(response.content!!)
                assertNotEquals("", result.error)
            }
        }

        @Test
        fun `must reject if username is already taken`() = runServer { _ ->
            registerRequest("user", "12345") {
                assertEquals(HttpStatusCode.OK, response.status())
            }
            registerRequest("user", "23456789") {
                assertEquals(HttpStatusCode.OK, response.status())
                val result = mapper.readValue<UnavailableUsernameException>(response.content!!)
                assertNotEquals("", result.error)
            }
        }

        private fun TestApplicationEngine.registerRequest(username: String, password: String, block: TestApplicationCall.() -> Unit) =
                with(handleRequest(HttpMethod.Post, "/users/register") {
                    addHeader("Content-Type", "application/json")
                    setBody("""{"username":"$username", "password": "$password"}""")
                }, block)
    }

    @Nested
    inner class `authenticate method` {
        @Test
        fun `sample good authentication`() = runServer { server ->
            server.register(UserCredentials("user", "12345"))
            authenticateRequest("user", "12345") {
                assertEquals(HttpStatusCode.OK, response.status())
                val result = mapper.readValue<AuthenticateResponse>(response.content!!)
                assert(result.result.isNotEmpty())
            }
        }

        @Test
        fun `authentication - wrong credentials`() = runServer { server ->
            server.register(UserCredentials("user", "12345"))
            authenticateRequest("user", "12245") {
                assertEquals(HttpStatusCode.OK, response.status())
                val result = mapper.readValue<WrongCredentialsException>(response.content!!)
                assert(result.error.isNotEmpty())
            }
            authenticateRequest("vser", "12345") {
                assertEquals(HttpStatusCode.OK, response.status())
                val result = mapper.readValue<WrongCredentialsException>(response.content!!)
                assert(result.error.isNotEmpty())
            }
        }

        @Test
        fun `authentication - protocol error on empty credentials`() = runServer { server ->
            server.register(UserCredentials("user", "12345"))
            authenticateRequest("", "12345") {
                assertEquals(HttpStatusCode.OK, response.status())
                val result = mapper.readValue<ProtocolErrorException>(response.content!!)
                assert(result.error.isNotEmpty())
            }
            authenticateRequest("user", "") {
                assertEquals(HttpStatusCode.OK, response.status())
                val result = mapper.readValue<ProtocolErrorException>(response.content!!)
                assert(result.error.isNotEmpty())
            }
        }

        private fun TestApplicationEngine.authenticateRequest(username: String, password: String, block: TestApplicationCall.() -> Unit) =
                with(handleRequest(HttpMethod.Post, "/users/authenticate") {
                    addHeader("Content-Type", "application/json")
                    setBody("""{"username":"$username", "password": "$password"}""")
                }, block)
    }

    private fun runServer(block: TestApplicationEngine.(Server) -> Unit) {
        val sampleSecret = "secret"
        val server = Server(sampleSecret, dataBase = connection())
        withTestApplication(RestHttpServer(server, sampleSecret).restServer(), test = { block(server) })
    }
}