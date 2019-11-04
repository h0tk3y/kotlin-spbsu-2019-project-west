package snailmail


import snailmail.core.*
import snailmail.server.Server
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ServerTest {
    private fun generateServerWithTwoUsers(block: (Server) -> Unit) {
        val server = Server().apply {
            register(UserCredentials("A", "aaaaa"))
            register(UserCredentials("B", "bbbbb"))
        }
        block(server)
    }

    private fun generateServerWithFourUsers(block: (Server) -> Unit) {
        val server = Server().apply {
            register(UserCredentials("A", "aaaaa"))
            register(UserCredentials("B", "bbbbb"))
            register(UserCredentials("C", "ccccc"))
            register(UserCredentials("D", "ddddd"))
        }
        block(server)
    }

    @Test
    fun `successful reg`() {
        val server = Server()

        assertEquals("user", server.register(UserCredentials("user", "qwerty")))
    }

    @Test
    fun `trying to reg twice with the same username`() {
        val server = Server()

        assertEquals("user", server.register(UserCredentials("user", "qwerty")))
        assertFailsWith<UnavailableUsernameException> { server.register(UserCredentials("user", "password")) }
    }

    @Test
    fun `unregistered user tries to auth`() {
        val server = Server()

        assertFailsWith<WrongCredentialsException> { server.authenticate(UserCredentials("user", "password")) }
    }

    @Test
    fun `successful reg and auth`() {
        val server = Server()
        val userCredentials = UserCredentials("user", "abacaba")

        assertEquals("user", server.register(userCredentials))
        assertEquals("user", server.authenticate(userCredentials))
    }

    @Test
    fun `search for existing users`() {
        generateServerWithFourUsers {
            server ->
                assertEquals("B", server.searchByUsername("A", "B")!!.username)
                assertEquals("A", server.searchByUsername("B", "A")!!.username)
        }
    }

    @Test
    fun `search for nonexistent user`() {
        val server = Server()

        server.register(UserCredentials("user", "abacaba"))

        assertNull(server.searchByUsername("user", "alice"))
    }

    @Test
    fun `personal chat contains its members`() {
        generateServerWithTwoUsers {
            server ->
                val chat = server.getPersonalChatWith("A", server.searchByUsername("A", "B")!!.id)
                assert(chat.hasMember(server.searchByUsername("A", "A")!!.id))
                assert(chat.hasMember(server.searchByUsername("B", "B")!!.id))
        }
    }

    @Test
    fun `identity of personal chats with the same user`() {
        generateServerWithTwoUsers {
            server ->
                val chat = server.getPersonalChatWith("A", server.searchByUsername("A", "B")!!.id)
                val chatDuplicate = server.getPersonalChatWith("A", server.searchByUsername("A", "B")!!.id)
                assertEquals(chat, chatDuplicate)
        }
    }

    @Test
    fun `personal self-chat`() {
        val server = Server()

        server.register(UserCredentials("user", "00000"))

        val chat = server.getPersonalChatWith("user", server.searchByUsername("user", "user")!!.id)

        assertEquals(server.searchByUsername("user", "user")!!.id, chat.person1)
        assertEquals(server.searchByUsername("user", "user")!!.id, chat.person2)
    }

    @Test
    fun `no available chats`() {
        val server = Server()

        server.register(UserCredentials("user", "abacaba"))

        assert(server.getAvailableChats("user").isEmpty())
    }

    @Test
    fun `3 available chats`() {
        generateServerWithFourUsers {
            server ->
                val correctAvailableChats = listOf("B", "C", "D").map {
                    server.getPersonalChatWith("A", server.searchByUsername("A", it)!!.id)
                }
                assertEquals(correctAvailableChats, server.getAvailableChats("A"))
        }
    }

    @Test
    fun `sending a message to another user`() {
        generateServerWithTwoUsers {
            server ->
                val chat = server.getPersonalChatWith("A", server.searchByUsername("A", "B")!!.id)
                val message = server.sendTextMessage("A", "<3", chat.id)
                assertEquals("<3", message.content)
        }
    }

    @Test
    fun `sending a message to yourself`() {
        val server = Server()

        server.register(UserCredentials("user", "abacaba"))

        val chat = server.getPersonalChatWith("user", server.searchByUsername("user", "user")!!.id)
        val message = server.sendTextMessage("user", "odna krov' odna dusha odno sp", chat.id)

        assertEquals("odna krov' odna dusha odno sp", message.content)
    }

    @Test
    fun `no messages in personal chat`() {
        generateServerWithTwoUsers {
            server ->
                val chat = server.getPersonalChatWith("A", server.searchByUsername("A", "B")!!.id)
            assert(server.getChatMessages("A", chat.id).isEmpty())
            assert(server.getChatMessages("B", chat.id).isEmpty())
        }
    }

    @Test
    fun `messages from personal chat`() {
        generateServerWithTwoUsers {
            server ->
                val chat = server.getPersonalChatWith("A", server.searchByUsername("A", "B")!!.id)
                with(server) {
                    sendTextMessage("A", "h e l l o", chat.id)
                    sendTextMessage("B", "#__#", chat.id)
                    sendTextMessage("A", "", chat.id)
                    sendTextMessage("A", "", chat.id)
                }
                val correctChatMessages = listOf("h e l l o", "#__#", "", "")
            assertEquals(correctChatMessages,
                    server.getChatMessages("A", chat.id).map { (it as TextMessage).content })
        }
    }

    @Test
    fun `messages from personal self-chat`() {
        val server = Server()

        server.register(UserCredentials("user", "abacaba"))

        val chat = server.getPersonalChatWith("user", server.searchByUsername("user", "user")!!.id)
        server.sendTextMessage("user", "_____", chat.id)

        assertEquals(1, server.getChatMessages("user", chat.id).size)
        assertEquals("_____",
                (server.getChatMessages("user", chat.id).first() as TextMessage).content)
    }

    @Test
    fun `group chat`() {
        generateServerWithFourUsers {
                server ->
                val members = listOf("B", "C", "D").map {
                    server.searchByUsername("A", it)!!.id
                }
                val groupChat = server.createGroupChat("A", "", members)
                assertEquals(server.searchByUsername("A", "A")!!.id, groupChat.owner)
                assertEquals(members, groupChat.members)
        }
    }

    @Test
    fun `group self-chat`() {
        val server = Server()

        server.register(UserCredentials("user", "abacaba"))

        val members = listOf(server.searchByUsername("user", "user")!!.id)
        val groupChat = server.createGroupChat("user", "", members)

        assertEquals(server.searchByUsername("user", "user")!!.id, groupChat.owner)
        assertEquals(members, groupChat.members)
    }

    @Test
    fun `getting available chats with group chat`() {
        generateServerWithFourUsers {
            server ->
                val chats = listOf("B", "C", "D").map {
                    server.getPersonalChatWith("A", server.searchByUsername("A", it)!!.id)
                }
                val members = listOf("B", "C", "D").map {
                    server.searchByUsername("A", it)!!.id
                }
                val chatABCD = server.createGroupChat("A", "", members)
                val correctAvailableChats = chats + chatABCD
            assertEquals(correctAvailableChats, server.getAvailableChats("A"))
        }
    }

    @Test
    fun `trying to get messages from personal chat by nonmember results in an exception`() {
        val server = Server().apply {
            register(UserCredentials("alice", "aaaaa"))
            register(UserCredentials("bob", "bbbbb"))
            register(UserCredentials("eve", "eeeee"))

        }

        val chat = server.getPersonalChatWith("alice", server.searchByUsername("alice", "bob")!!.id)

        assertFailsWith<UserIsNotMemberException> { server.getChatMessages("eve", chat.id) }
    }

    @Test
    fun `getting available chats with invalid token results in an exception`() {
        val server = Server()

        assertFailsWith<InvalidTokenException> { server.getAvailableChats("user") }
    }

    @Test
    fun `searching by username with invalid token results in an exception`() {
        val server = Server()

        server.register(UserCredentials("userRegistered", "abacaba"))

        assertFailsWith<InvalidTokenException> { server.searchByUsername("user", "userRegistered") }
    }

    @Test
    fun `getting personal chat with invalid token results in an exception`() {
        val server = Server()

        server.register(UserCredentials("userRegistered", "abacaba"))
        val userRegistered = server.searchByUsername("userRegistered", "userRegistered")!!

        assertFailsWith<InvalidTokenException> { server.getPersonalChatWith("user", userRegistered.id) }
    }

    @Test
    fun `sending message with invalid token results in an exception`() {
        generateServerWithTwoUsers {
            server ->
                val chat = server.getPersonalChatWith("A", server.searchByUsername("A", "B")!!.id)
                assertFailsWith<InvalidTokenException> { server.sendTextMessage("C", "_", chat.id) }
        }
    }

    @Test
    fun `getting messages from personal chat with invalid token results in an exception`() {
        generateServerWithTwoUsers {
            server ->
                val chat = server.getPersonalChatWith("A", server.searchByUsername("A", "B")!!.id)
                assertFailsWith<InvalidTokenException> { server.getChatMessages("C", chat.id) }
        }
    }

    @Test
    fun `creating group chat with invalid token results in an exception`() {
        generateServerWithTwoUsers {
            server ->
                val userA = server.searchByUsername("A", "A")!!
                val userB = server.searchByUsername("B", "B")!!
                val members = listOf(userA.id, userB.id)
                assertFailsWith<InvalidTokenException> { server.createGroupChat("C", "", members) }
        }
    }
}