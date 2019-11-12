package snailmail

import org.junit.Ignore
import snailmail.client.Client
import snailmail.client.NotAuthenticatedException
import snailmail.client.UserNotFoundException
import snailmail.core.TextMessage
import snailmail.core.UserCredentials
import snailmail.server.Server
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Ignore
internal class ClientTest {
    private fun generateTwoUsers(block: (userA: Client, userB: Client) -> Unit) {
        val server = Server()
        val userA = Client(server)
        val userB = Client(server)

        userA.register(UserCredentials("A", "aaaaa"))
        userB.register(UserCredentials("B", "bbbbb"))

        block(userA, userB)
    }

    @Test
    fun `successful reg`() {
        val server = Server()
        val user = Client(server)

        user.register(UserCredentials("user", "12345")) // shouldn't throw exceptions
    }
    
    @Test
    fun `successful reg and auth`() {
        val server = Server()
        val user = Client(server)

        user.register(UserCredentials("user", "abacaba")) // shouldn't throw exceptions
        user.authenticate(UserCredentials("user", "abacaba")) // shouldn't throw exceptions
    }
    
    @Test
    fun `try to auth from another client`() {
        val server = Server()
        val user = Client(server)
        val userAnotherClient = Client(server)

        user.register(UserCredentials("user", "abacaba")) // shouldn't throw exceptions
        userAnotherClient.authenticate(UserCredentials("user", "abacaba")) // shouldn't throw exceptions
    }
    
    /*
    @Test
    fun `trying to reg twice with the same username`() {
        val server = Server()
        val user = Client(server)

        assert(user.register(UserCredentials("user", "12345")))
        assert(!user.register(UserCredentials("user", "qwerty")))
    }

    @Test
    fun `trying to reg with taken username`() {
        val server = Server()
        val user = Client(server)
        val userDuplicate = Client(server)

        assert(user.register(UserCredentials("user", "12345")))
        assert(!userDuplicate.register(UserCredentials("user", "qwerty")))
    }

    @Test
    fun `unregistered user tries to auth`() {
        val server = Server()
        val user = Client(server)

        assert(!user.authenticate(UserCredentials("user", "00000")))
    }

    @Test
    fun `typo in password`() {
        val server = Server()
        val user = Client(server)

        assert(user.register(UserCredentials("user", "abacaba")))
        assert(!user.authenticate(UserCredentials("user", "abacabz")))
    }

    @Test
    fun `typo in username`() {
        val server = Server()
        val user = Client(server)

        assert(user.register(UserCredentials("user", "abacaba")))
        assert(!user.authenticate(UserCredentials("usir", "abacaba")))
    }
    */

    @Test
    fun `finding yourself`() {
        val server = Server()
        val user = Client(server)

        user.register(UserCredentials("uSER2000", "user000"))

        assertEquals("uSER2000", user.findUser("uSER2000").username)
    }

    @Test
    fun `A and B is looking for each other`() {
        generateTwoUsers { userA, userB ->
            assertEquals("B", userA.findUser("B").username)
            assertEquals("A", userB.findUser("A").username)
        }
    }

    @Test
    fun `A sends a message to B`() {
        generateTwoUsers { userA, _ ->
            assertEquals("hello B", userA.sendMessage("B", "hello B").content)
        }
    }

    @Test
    fun `sending empty message`() {
        generateTwoUsers { userA, _ ->
            assertEquals("", userA.sendMessage("B", "").content)
        }
    }

    @Test
    fun `sending a pair of messages to each other`() {
        generateTwoUsers { userA, userB ->
            assertEquals("hello", userA.sendMessage("B", "hello").content)
            assertEquals("bye", userB.sendMessage("A", "bye").content)
        }
    }

    @Test
    fun `single message in personal chat history`() {
        generateTwoUsers { userA, userB ->
            userA.sendMessage("B", "hello")
            assertEquals(1, userB.getPersonalChatHistory("A").size)
            assertEquals("hello", (userB.getPersonalChatHistory("B").first() as TextMessage).content)
        }
    }

    @Test
    fun `no available chats`() {
        val server = Server()
        val user = Client(server)

        user.register(UserCredentials("user", "abacaba"))
        assert(user.findAvailableChats().isEmpty())
    }

    @Test
    fun `only one is available`() {
        val server = Server()
        val user = Client(server).apply {
            register(UserCredentials("user", "abacaba"))
            sendMessage("user", "=)")
        }

        assertEquals(1, user.findAvailableChats().size)
    }

    @Test
    fun `3 available chats`() {
        val server = Server()
        val userA = Client(server)
        val userB = Client(server)
        val userC = Client(server)
        val userD = Client(server)

        userA.register(UserCredentials("A", "aaaaa"))
        userB.register(UserCredentials("B", "bbbbb"))
        userC.register(UserCredentials("C", "ccccc"))
        userD.register(UserCredentials("D", "ddddd"))

        with(userA) {
            sendMessage("B", "hello!!")
            sendMessage("C", "hello!!")
            sendMessage("D", "hello!!")
        }


        assertEquals(3, userA.findAvailableChats().size)
    }

    @Test
    fun `empty history of personal chat`() {
        generateTwoUsers { userA, userB ->
            assert(userA.getPersonalChatHistory("B").isEmpty())
            assert(userB.getPersonalChatHistory("A").isEmpty())
        }
    }

    @Test
    fun `nonempty history of personal chat`() {
        generateTwoUsers { userA, userB ->
            userA.sendMessage("B", "aa))")
            userB.sendMessage("A", "bb((")
            userA.sendMessage("B", "a)")
            userB.sendMessage("A", "b(")
            userA.sendMessage("B", "aaaaaa)))")

            val correctChatHistory = listOf("aa))", "bb((", "a)", "b(", "aaaaaa)))")
            assertEquals(correctChatHistory, userA.getPersonalChatHistory("B").map { (it as TextMessage).content })
            assertEquals(correctChatHistory, userB.getPersonalChatHistory("A").map { (it as TextMessage).content })
        }
    }

    @Test
    fun `mess mess messages in history of personal chat`() {
        generateTwoUsers { userA, userB ->
            userA.sendMessage("B", "hel")
            userA.sendMessage("B", "lo")
            userA.sendMessage("B", ")))")
            userB.sendMessage("A", "=__=")
            userA.sendMessage("B", "x__x")

            val correctChatHistory = listOf("hel", "lo", ")))", "=__=", "x__x")
            assertEquals(correctChatHistory, userA.getPersonalChatHistory("B").map { (it as TextMessage).content })
            assertEquals(correctChatHistory, userB.getPersonalChatHistory("A").map { (it as TextMessage).content })
        }
    }

    @Test
    fun `trying to send a message by an unauthorized user results in an exception`() {
        val server = Server()
        val user = Client(server)
        val userNoAuth = Client(server)

        user.register(UserCredentials("user", "abacaba"))

        assertFailsWith<NotAuthenticatedException> { userNoAuth.sendMessage("user", "hi!") }
    }

    @Test
    fun `trying to find available chats by an unauthorized user results in an exception`() {
        val server = Server()
        val userNoAuth = Client(server)

        assertFailsWith<NotAuthenticatedException> { userNoAuth.findAvailableChats() }
    }

    @Test
    fun `trying to get history of personal chat by an unauthorized user results in an exception`() {
        val server = Server()
        val user = Client(server)
        val userNoAuth = Client(server)

        user.register(UserCredentials("user", "abacaba"))

        assertFailsWith<NotAuthenticatedException> { userNoAuth.getPersonalChatHistory("user") }
    }

    @Test
    fun `trying to find a user by an unauthorized user results in an exception`() {
        val server = Server()
        val user = Client(server)
        val userNoAuth = Client(server)

        user.register(UserCredentials("user", "abacaba"))

        assertFailsWith<NotAuthenticatedException> { userNoAuth.findUser("user") }
    }

    @Test
    fun `trying to find nonexistent user results in an exception`() {
        val server = Server()
        val user = Client(server)

        user.register(UserCredentials("user", "abacaba"))

        assertFailsWith<UserNotFoundException> { user.findUser("Alice") }
    }

    @Test
    fun `trying to send message to nonexistent user results in an exception`() {
        val server = Server()
        val user = Client(server)

        user.register(UserCredentials("user", "abacaba"))

        assertFailsWith<UserNotFoundException> { user.sendMessage("Alice", "<3") }
    }

    @Test
    fun `trying to get history of personal chat with nonexistent user results in an exception`() {
        val server = Server()
        val user = Client(server)

        user.register(UserCredentials("user", "abacaba"))

        assertFailsWith<UserNotFoundException> { user.getPersonalChatHistory("Alice") }
    }
}
