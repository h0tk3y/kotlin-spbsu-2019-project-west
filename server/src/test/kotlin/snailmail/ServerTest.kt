package snailmail


import snailmail.core.*
import snailmail.server.Server
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class ServerTest {
    private fun generateServerWithTwoUsers(block: (Server, AuthToken, AuthToken) -> Unit) {
        val server = Server()
        val authA = server.register(UserCredentials("A", "aaaaa"))
        val authB = server.register(UserCredentials("B", "bbbbb"))
        block(server, authA, authB)
    }

    private fun generateServerWithFourUsers(block: (Server, AuthToken, AuthToken, AuthToken, AuthToken) -> Unit) {
        val server = Server()
        val authA = server.register(UserCredentials("A", "aaaaa"))
        val authB = server.register(UserCredentials("B", "bbbbb"))
        val authC = server.register(UserCredentials("C", "ccccc"))
        val authD = server.register(UserCredentials("D", "ddddd"))
        block(server, authA, authB, authC, authD)
    }

    @Test
    fun `successful reg`() {
        val server = Server()

        assert(server.register(UserCredentials("user", "qwerty")).isNotEmpty())
    }

    @Test
    fun `trying to reg twice with the same username`() {
        val server = Server()

        assert(server.register(UserCredentials("user", "qwerty")).isNotEmpty())
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

        assert(server.register(userCredentials).isNotEmpty())
        assert(server.authenticate(userCredentials).isNotEmpty())
    }

    @Test
    fun `search for existing users`() {
        generateServerWithFourUsers { server, tokenA, tokenB, _, _ ->
            assertEquals("B", server.getUserByUsername(tokenA, "B").username)
            assertEquals("A", server.getUserByUsername(tokenB, "A").username)
        }
    }

    @Test
    fun `search for nonexistent user`() {
        val server = Server()
        val token = server.register(UserCredentials("user", "abacaba"))

        assertFailsWith<UserDoesNotExistException> { server.getUserByUsername(token, "alice") }
    }

    @Test
    fun `personal chat contains its members`() {
        generateServerWithTwoUsers { server, tokenA, tokenB ->
            val chat = server.getPersonalChatWith(tokenA, server.getUserByUsername(tokenA, "B").id)
            assert(chat.hasMember(server.getUserByUsername(tokenA, "A").id))
            assert(chat.hasMember(server.getUserByUsername(tokenB, "B").id))
        }
    }

    @Test
    fun `identity of personal chats with the same user`() {
        generateServerWithTwoUsers { server, tokenA, _ ->
            val chat = server.getPersonalChatWith(tokenA, server.getUserByUsername(tokenA, "B").id)
            val chatDuplicate = server.getPersonalChatWith(tokenA, server.getUserByUsername(tokenA, "B").id)
            assertEquals(chat, chatDuplicate)
        }
    }

    @Test
    fun `personal self-chat`() {
        val server = Server()
        val token = server.register(UserCredentials("user", "00000"))

        val chat = server.getPersonalChatWith(token, server.getUserByUsername(token, "user").id)
        assertEquals(server.getUserByUsername(token, "user").id, chat.person1)
        assertEquals(server.getUserByUsername(token, "user").id, chat.person2)
    }

    @Test
    fun `no chats`() {
        val server = Server()
        val token = server.register(UserCredentials("user", "abacaba"))
        assert(server.getChats(token).isEmpty())
    }

    @Test
    fun `3 chats`() {
        generateServerWithFourUsers { server, tokenA, _, _, _ ->
            val correctChats = listOf("B", "C", "D").map {
                server.getPersonalChatWith(tokenA, server.getUserByUsername(tokenA, it).id)
            }
            assertEquals(correctChats, server.getChats(tokenA))
        }
    }

    @Test
    fun `sending a message to another user`() {
        generateServerWithTwoUsers { server, tokenA, _ ->
            val chat = server.getPersonalChatWith(tokenA, server.getUserByUsername(tokenA, "B").id)
            val message = server.sendTextMessage(tokenA, "<3", chat.id)
            assertEquals("<3", message.content)
        }
    }

    @Test
    fun `sending a message to yourself`() {
        val server = Server()
        val token = server.register(UserCredentials("user", "abacaba"))

        val chat = server.getPersonalChatWith(token, server.getUserByUsername(token, "user").id)
        val message = server.sendTextMessage(token, "odna krov' odna dusha odno sp", chat.id)
        assertEquals("odna krov' odna dusha odno sp", message.content)
    }

    @Test
    fun `no messages in personal chat`() {
        generateServerWithTwoUsers { server, tokenA, tokenB ->
            val chat = server.getPersonalChatWith(tokenA, server.getUserByUsername(tokenA, "B").id)
            assert(server.getChatMessages(tokenA, chat.id).isEmpty())
            assert(server.getChatMessages(tokenB, chat.id).isEmpty())
        }
    }

    @Test
    fun `messages from personal chat`() {
        generateServerWithTwoUsers { server, tokenA, tokenB ->
            val chat = server.getPersonalChatWith(tokenA, server.getUserByUsername(tokenA, "B").id)
            with(server) {
                sendTextMessage(tokenA, "h e l l o", chat.id)
                sendTextMessage(tokenB, "#__#", chat.id)
                sendTextMessage(tokenA, "", chat.id)
                sendTextMessage(tokenA, "", chat.id)
            }
            val correctChatMessages = listOf("h e l l o", "#__#", "", "")
            assertEquals(correctChatMessages, server.getChatMessages(tokenA, chat.id).map { (it as TextMessage).content })
        }
    }

    @Test
    fun `messages from personal self-chat`() {
        val server = Server()
        val token = server.register(UserCredentials("user", "abacaba"))
        val chat = server.getPersonalChatWith(token, server.getUserByUsername(token, "user").id)
        server.sendTextMessage(token, "_____", chat.id)

        assertEquals(1, server.getChatMessages(token, chat.id).size)
        assertEquals("_____", (server.getChatMessages(token, chat.id).first() as TextMessage).content)
    }

    @Test
    fun `group chat`() {
        generateServerWithFourUsers { server, tokenA, _, _, _ ->
            val members = listOf("B", "C", "D").map {
                server.getUserByUsername(tokenA, it).id
            }
            val groupChat = server.createGroupChat(tokenA, "", members)
            assertEquals(server.getUserByUsername(tokenA, "A").id, groupChat.owner)
            assertEquals(members, groupChat.members)
        }
    }

    @Test
    fun `group self-chat`() {
        val server = Server()
        val token = server.register(UserCredentials("user", "abacaba"))

        val members = listOf(server.getUserByUsername(token, "user").id)
        val groupChat = server.createGroupChat(token, "", members)

        assertEquals(server.getUserByUsername(token, "user").id, groupChat.owner)
        assertEquals(members, groupChat.members)
    }

    @Test
    fun `getting chats with group chat`() {
        generateServerWithFourUsers { server, tokenA, _, _, _ ->
            val chats = listOf("B", "C", "D").map {
                server.getPersonalChatWith(tokenA, server.getUserByUsername(tokenA, it).id)
            }
            val members = listOf("B", "C", "D").map {
                server.getUserByUsername(tokenA, it).id
            }
            val chatABCD = server.createGroupChat(tokenA, "", members)
            val correctChats = chats + chatABCD

            assertEquals(correctChats, server.getChats(tokenA))
        }
    }

    @Test
    fun `trying to get messages from personal chat by nonmember results in an exception`() {
        val server = Server()

        val tokenA = server.register(UserCredentials("alice", "aaaaa"))
        server.register(UserCredentials("bob", "bbbbb"))
        val tokenE = server.register(UserCredentials("eve", "eeeee"))

        val chat = server.getPersonalChatWith(tokenA, server.getUserByUsername(tokenA, "bob").id)

        assertFailsWith<UserIsNotMemberException> { server.getChatMessages(tokenE, chat.id) }
    }

    @Test
    fun `getting chats with invalid token results in an exception`() {
        val server = Server()
        assertFailsWith<InvalidTokenException> { server.getChats("user") }
    }

    @Test
    fun `searching by username with invalid token results in an exception`() {
        val server = Server()
        server.register(UserCredentials("userRegistered", "abacaba"))

        assertFailsWith<InvalidTokenException> { server.getUserByUsername("user", "userRegistered") }
    }

    @Test
    fun `getting personal chat with invalid token results in an exception`() {
        val server = Server()

        val token = server.register(UserCredentials("userRegistered", "abacaba"))
        val userRegistered = server.getUserByUsername(token, "userRegistered")

        assertFailsWith<InvalidTokenException> { server.getPersonalChatWith("user", userRegistered.id) }
    }

    @Test
    fun `sending message with invalid token results in an exception`() {
        generateServerWithTwoUsers { server, tokenA, _ ->
            val chat = server.getPersonalChatWith(tokenA, server.getUserByUsername(tokenA, "B").id)
            assertFailsWith<InvalidTokenException> { server.sendTextMessage("C", "_", chat.id) }
        }
    }

    @Test
    fun `getting messages from personal chat with invalid token results in an exception`() {
        generateServerWithTwoUsers { server, tokenA, _ ->
            val chat = server.getPersonalChatWith(tokenA, server.getUserByUsername(tokenA, "B").id)
            assertFailsWith<InvalidTokenException> { server.getChatMessages("C", chat.id) }
        }
    }

    @Test
    fun `creating group chat with invalid token results in an exception`() {
        generateServerWithTwoUsers { server, tokenA, tokenB ->
            val userA = server.getUserByUsername(tokenA, "A")
            val userB = server.getUserByUsername(tokenB, "B")
            val members = listOf(userA.id, userB.id)
            assertFailsWith<InvalidTokenException> { server.createGroupChat("C", "", members) }
        }
    }
}