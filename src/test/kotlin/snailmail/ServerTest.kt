package snailmail


import snailmail.core.InvalidTokenException
import snailmail.core.TextMessage
import snailmail.core.UserCredentials
import snailmail.core.UserIsNotMemberException
import snailmail.core.api.AuthRegisterFailed
import snailmail.core.api.AuthSuccessful
import snailmail.core.api.AuthWrongCredentials
import snailmail.server.Server
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ServerTest {
    private fun generateServerWithTwoUsers(block: (Server) -> Unit) {
        val server = Server()
        server.register(UserCredentials("A", "aaaaa"))
        server.register(UserCredentials("B", "bbbbb"))
        block(server)
    }

    private fun generateServerWithFourUsers(block: (Server) -> Unit) {
        val server = Server()
        server.register(UserCredentials("A", "aaaaa"))
        server.register(UserCredentials("B", "bbbbb"))
        server.register(UserCredentials("C", "ccccc"))
        server.register(UserCredentials("D", "ddddd"))
        block(server)
    }

    @Test
    fun `successful reg`() {
        val server = Server()

        assert(server.register(UserCredentials("user", "qwerty")) is AuthSuccessful)
    }

    @Test
    fun `trying to reg twice with the same username`() {
        val server = Server()

        assert(server.register(UserCredentials("user", "qwerty")) is AuthSuccessful)
        assert(server.register(UserCredentials("user", "password")) is AuthRegisterFailed)
    }

    @Test
    fun `unregistered user tries to auth`() {
        val server = Server()

        assert(server.authenticate(UserCredentials("user", "qwerty")) is AuthWrongCredentials)
    }

    @Test
    fun `successful reg and auth`() {
        val server = Server()
        val userCredentials = UserCredentials("user", "abacaba")

        assert(server.register(userCredentials) is AuthSuccessful)
        assert(server.authenticate(userCredentials) is AuthSuccessful)
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

        assert(server.getAvailableChats("user").getChats().isEmpty())
    }

    @Test
    fun `3 available chats`() {
        generateServerWithFourUsers {
            server ->
                val chatWithB = server.getPersonalChatWith("A", server.searchByUsername("A", "B")!!.id)
                val chatWithC = server.getPersonalChatWith("A", server.searchByUsername("A", "C")!!.id)
                val chatWithD = server.getPersonalChatWith("A", server.searchByUsername("A", "D")!!.id)
                val correctAvailableChats = listOf(chatWithB, chatWithC, chatWithD)
                assertEquals(correctAvailableChats, server.getAvailableChats("A").getChats())
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
                assert(server.getChatMessages("A", chat.id).getMessages().isEmpty())
                assert(server.getChatMessages("B", chat.id).getMessages().isEmpty())
        }
    }

    @Test
    fun `messages from personal chat`() {
        generateServerWithTwoUsers {
            server ->
                val chat = server.getPersonalChatWith("A", server.searchByUsername("A", "B")!!.id)
                server.sendTextMessage("A", "h e l l o", chat.id)
                server.sendTextMessage("B", "#__#", chat.id)
                server.sendTextMessage("A", "", chat.id)
                server.sendTextMessage("A", "", chat.id)
                val correctChatMessages = listOf("h e l l o", "#__#", "", "")
                assertEquals(correctChatMessages,
                        server.getChatMessages("A", chat.id).getMessages().map { (it as TextMessage).content })
        }
    }

    @Test
    fun `messages from personal self-chat`() {
        val server = Server()

        server.register(UserCredentials("user", "abacaba"))

        val chat = server.getPersonalChatWith("user", server.searchByUsername("user", "user")!!.id)
        server.sendTextMessage("user", "_____", chat.id)

        assertEquals(1, server.getChatMessages("user", chat.id).getMessages().size)
        assertEquals("_____",
                (server.getChatMessages("user", chat.id).getMessages().first() as TextMessage).content)
    }

    @Test
    fun `group chat`() {
        generateServerWithFourUsers {
                server ->
                val members = listOf(server.searchByUsername("A", "B")!!.id,
                        server.searchByUsername("A", "C")!!.id,
                        server.searchByUsername("A", "D")!!.id)
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
                val chatWithB = server.getPersonalChatWith("A", server.searchByUsername("A", "B")!!.id)
                val chatWithC = server.getPersonalChatWith("A", server.searchByUsername("A", "C")!!.id)
                val chatWithD = server.getPersonalChatWith("A", server.searchByUsername("A", "D")!!.id)
                val members = listOf(server.searchByUsername("A", "B")!!.id,
                        server.searchByUsername("A", "C")!!.id,
                        server.searchByUsername("A", "D")!!.id)
                val chatABCD = server.createGroupChat("A", "", members)
                val correctAvailableChats = listOf(chatWithB, chatWithC, chatWithD, chatABCD)
                assertEquals(correctAvailableChats, server.getAvailableChats("A").getChats())
        }
    }

    @Test
    fun `trying to get messages from personal chat by nonmember results in an exception`() {
        val server = Server()

        server.register(UserCredentials("alice", "aaaaa"))
        server.register(UserCredentials("bob", "bbbbb"))
        server.register(UserCredentials("eve", "eeeee"))

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