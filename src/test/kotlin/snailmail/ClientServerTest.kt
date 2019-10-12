/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package snailmail

import snailmail.client.Client
import snailmail.core.TextMessage
import snailmail.core.UserCredentials
import snailmail.server.Server
import kotlin.test.*

class ClientServerTest {
    @Test
    fun `client-server simple integrity test`() {
        val server = Server()
        val alice = Client(server)
        val bob = Client(server)

        assert(alice.register(UserCredentials("alice", "alice123")))
        assert(bob.register(UserCredentials("bob", "bob345")))

        assertEquals(emptyList(), alice.findAvailableChats())
        assertEquals(emptyList(), bob.findAvailableChats())

        assertEquals(emptyList(), alice.getPersonalChatHistory("alice"))

        alice.sendMessage("bob", "Hi, bob!")
        assertEquals(1, bob.getPersonalChatHistory("alice").size)
        val msg = bob.getPersonalChatHistory("alice").first()
        assertEquals("Hi, bob!", (msg as TextMessage).content)
    }
}
