package snailmail


import snailmail.core.InvalidTokenException
import snailmail.core.TextMessage
import snailmail.core.UserCredentials
import snailmail.core.UserIsNotMemberException
import snailmail.core.api.APITransportMapping
import snailmail.core.api.AuthRegisterFailed
import snailmail.core.api.AuthSuccessful
import snailmail.core.api.AuthWrongCredentials
import snailmail.server.Server
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ServerTest {
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
        val server = Server()

        val authResA = server.register(UserCredentials("A", "aaaaa"))
        val tokenA = if (authResA is AuthSuccessful) authResA.token else null
        val authResB = server.register(UserCredentials("B", "bbbbb"))
        val tokenB = if (authResB is AuthSuccessful) authResB.token else null

        assert(tokenA != null && "B" == server.searchByUsername(tokenA, "B")!!.username)
        assert(tokenB != null && "A" == server.searchByUsername(tokenB, "A")!!.username)
    }

    @Test
    fun `search for nonexistent user`() {
        val server = Server()

        val authRes = server.register(UserCredentials("user", "abacaba"))
        val token = if (authRes is AuthSuccessful) authRes.token else null
        assert(token != null && server.searchByUsername(token, "alice") == null)
    }

    @Test
    fun `personal chat contains its members`() {
        val server = Server()

        val authResA = server.register(UserCredentials("A", "aaaaa"))
        val tokenA = if (authResA is AuthSuccessful) authResA.token else null
        val authResB = server.register(UserCredentials("B", "bbbbb"))
        val tokenB = if (authResB is AuthSuccessful) authResB.token else null

        if (tokenA != null && tokenB != null) {
            val chat = server.getPersonalChatWith(tokenA, server.searchByUsername(tokenA, "B")!!.id)

            assert(chat.hasMember(server.searchByUsername(tokenA, "A")!!.id))
            assert(chat.hasMember(server.searchByUsername(tokenB, "B")!!.id))
        } else
            assert(false)
    }

    @Test
    fun `identity of personal chats with the same user`() {
        val server = Server()

        val authResA = server.register(UserCredentials("A", "aaaaa"))
        val tokenA = if (authResA is AuthSuccessful) authResA.token else null
        server.register(UserCredentials("B", "bbbbb"))

        if (tokenA != null) {
            val chat = server.getPersonalChatWith(tokenA, server.searchByUsername(tokenA, "B")!!.id)
            val chatDuplicate = server.getPersonalChatWith(tokenA, server.searchByUsername(tokenA, "B")!!.id)

            assertEquals(chat, chatDuplicate)
        } else {
            assert(false)
        }
    }

    @Test
    fun `personal self-chat`() {
        val server = Server()

        val authRes = server.register(UserCredentials("user", "00000"))
        val token = if (authRes is AuthSuccessful) authRes.token else null

        if (token != null) {
            val chat = server.getPersonalChatWith(token, server.searchByUsername(token, "user")!!.id)

            assertEquals(server.searchByUsername(token, "user")!!.id, chat.person1)
            assertEquals(server.searchByUsername(token, "user")!!.id, chat.person2)
        } else {
            assert(false)
        }
    }

    @Test
    fun `no available chats`() {
        val server = Server()

        val authRes = server.register(UserCredentials("user", "abacaba"))
        val token = if (authRes is AuthSuccessful) authRes.token else null

        assert(token != null && server.getAvailableChats(token).getChats().isEmpty())
    }

    @Test
    fun `3 available chats`() {
        val server = Server()

        val authResA = server.register(UserCredentials("A", "aaaaa"))
        val tokenA = if (authResA is AuthSuccessful) authResA.token else null
        server.register(UserCredentials("B", "bbbbb"))
        server.register(UserCredentials("C", "ccccc"))
        server.register(UserCredentials("D", "ddddd"))

        if (tokenA != null) {
            val chatWithB = server.getPersonalChatWith(tokenA, server.searchByUsername(tokenA, "B")!!.id)
            val chatWithC = server.getPersonalChatWith(tokenA, server.searchByUsername(tokenA, "C")!!.id)
            val chatWithD = server.getPersonalChatWith(tokenA, server.searchByUsername(tokenA, "D")!!.id)

            val correctAvailableChats = listOf(chatWithB, chatWithC, chatWithD)

            assertEquals(correctAvailableChats, server.getAvailableChats(tokenA).getChats())
        } else
            assert(false)
    }

    @Test
    fun `sending a message to another user`() {
        val server = Server()


        val authResA = server.register(UserCredentials("A", "aaaaa"))
        val tokenA = if (authResA is AuthSuccessful) authResA.token else null
        server.register(UserCredentials("B", "bbbbb"))

        if (tokenA != null) {
            val chat = server.getPersonalChatWith(tokenA, server.searchByUsername(tokenA, "B")!!.id)
            val message = server.sendTextMessage(tokenA, "<3", chat.id)

            assertEquals("<3", message.content)
        } else
            assert(false)
    }

    @Test
    fun `sending a message to yourself`() {
        val server = Server()

        val authRes = server.register(UserCredentials("user", "abacaba"))
        val token = if (authRes is AuthSuccessful) authRes.token else null

        if (token != null) {
            val chat = server.getPersonalChatWith(token, server.searchByUsername(token, "user")!!.id)
            val message = server.sendTextMessage(token, "odna krov' odna dusha odno sp", chat.id)

            assertEquals("odna krov' odna dusha odno sp", message.content)
        } else
            assert(false)
    }

    @Test
    fun `no messages in personal chat`() {
        val server = Server()

        val authResA = server.register(UserCredentials("A", "aaaaa"))
        val tokenA = if (authResA is AuthSuccessful) authResA.token else null
        val authResB = server.register(UserCredentials("B", "bbbbb"))
        val tokenB = if (authResB is AuthSuccessful) authResB.token else null

        if (tokenA != null && tokenB != null) {
            val chat = server.getPersonalChatWith(tokenA, server.searchByUsername(tokenA, "B")!!.id)

            assert(server.getChatMessages(tokenA, chat.id).getMessages().isEmpty())
            assert(server.getChatMessages(tokenB, chat.id).getMessages().isEmpty())
        } else
            assert(false)
    }

    @Test
    fun `messages from personal chat`() {
        val server = Server()

        val authResA = server.register(UserCredentials("A", "aaaaa"))
        val tokenA = if (authResA is AuthSuccessful) authResA.token else null
        val authResB = server.register(UserCredentials("B", "bbbbb"))
        val tokenB = if (authResB is AuthSuccessful) authResB.token else null

        if (tokenA != null && tokenB != null) {
            val chat = server.getPersonalChatWith(tokenA, server.searchByUsername(tokenA, "B")!!.id)

            server.sendTextMessage(tokenA, "h e l l o", chat.id)
            server.sendTextMessage(tokenB, "#__#", chat.id)
            server.sendTextMessage(tokenA, "", chat.id)
            server.sendTextMessage(tokenA, "", chat.id)

            val correctChatMessages = listOf("h e l l o", "#__#", "", "")

            assertEquals(correctChatMessages,
                server.getChatMessages(tokenA, chat.id).getMessages().map { (it as TextMessage).content })
        } else
            assert(false)
    }

    @Test
    fun `messages from personal self-chat`() {
        val server = Server()

        val authRes = server.register(UserCredentials("user", "abacaba"))
        val token = if (authRes is AuthSuccessful) authRes.token else null

        if (token != null) {
            val chat = server.getPersonalChatWith(token, server.searchByUsername(token, "user")!!.id)
            server.sendTextMessage(token, "_____", chat.id)

            assertEquals(1, server.getChatMessages(token, chat.id).getMessages().size)
            assertEquals(
                "_____",
                (server.getChatMessages(token, chat.id).getMessages().first() as TextMessage).content
            )
        } else
            assert(false)
    }

    @Test
    fun `group chat`() {
        val server = Server()

        val authResA = server.register(UserCredentials("A", "aaaaa"))
        val tokenA = if (authResA is AuthSuccessful) authResA.token else null
        server.register(UserCredentials("B", "bbbbb"))
        server.register(UserCredentials("C", "ccccc"))
        server.register(UserCredentials("D", "ddddd"))

        if (tokenA != null) {
            val members = listOf(
                server.searchByUsername(tokenA, "B")!!.id,
                server.searchByUsername(tokenA, "C")!!.id,
                server.searchByUsername(tokenA, "D")!!.id
            )

            val groupChat = server.createGroupChat(tokenA, "", members)

            assertEquals(server.searchByUsername(tokenA, "A")!!.id, groupChat.owner)
            assertEquals(members, groupChat.members)
        } else
            assert(false)
    }

    @Test
    fun `group self-chat`() {
        val server = Server()

        val authRes = server.register(UserCredentials("user", "abacaba"))
        val token = if (authRes is AuthSuccessful) authRes.token else null

        if (token != null) {
            val members = listOf(server.searchByUsername(token, "user")!!.id)
            val groupChat = server.createGroupChat(token, "", members)

            assertEquals(server.searchByUsername(token, "user")!!.id, groupChat.owner)
            assertEquals(members, groupChat.members)
        } else
            assert(false)
    }

    @Test
    fun `getting available chats with group chat`() {
        val server = Server()

        val authResA = server.register(UserCredentials("A", "aaaaa"))
        val tokenA = if (authResA is AuthSuccessful) authResA.token else null
        server.register(UserCredentials("B", "bbbbb"))
        server.register(UserCredentials("C", "ccccc"))
        server.register(UserCredentials("D", "ddddd"))

        if (tokenA != null) {
            val chatWithB = server.getPersonalChatWith(tokenA, server.searchByUsername(tokenA, "B")!!.id)
            val chatWithC = server.getPersonalChatWith(tokenA, server.searchByUsername(tokenA, "C")!!.id)
            val chatWithD = server.getPersonalChatWith(tokenA, server.searchByUsername(tokenA, "D")!!.id)

            val members = listOf(
                server.searchByUsername(tokenA, "B")!!.id,
                server.searchByUsername(tokenA, "C")!!.id,
                server.searchByUsername(tokenA, "D")!!.id
            )

            val chatABCD = server.createGroupChat(tokenA, "", members)

            val correctAvailableChats = listOf(chatWithB, chatWithC, chatWithD, chatABCD)

            assertEquals(correctAvailableChats, server.getAvailableChats(tokenA).getChats())
        } else
            assert(false)
    }

    @Test
    fun `trying to get messages from personal chat by nonmember results in an exception`() {
        val server = Server()

        val authResAlice = server.register(UserCredentials("alice", "aaaaa"))
        val tokenAlice = if (authResAlice is AuthSuccessful) authResAlice.token else null
        server.register(UserCredentials("bob", "bbbbb"))
        val authResEve = server.register(UserCredentials("eve", "eeeee"))
        val tokenEve = if (authResEve is AuthSuccessful) authResEve.token else null

        if (tokenAlice != null && tokenEve != null) {
            val chat = server.getPersonalChatWith(tokenAlice, server.searchByUsername(tokenAlice, "bob")!!.id)

            assertFailsWith<UserIsNotMemberException> { server.getChatMessages(tokenEve, chat.id) }
        } else
            assert(false)
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

        val authRes = server.register(UserCredentials("userRegistered", "abacaba"))
        val token = if (authRes is AuthSuccessful) authRes.token else null

        if (token != null) {
            val userRegistered = server.searchByUsername(token, "userRegistered")!!

            assertFailsWith<InvalidTokenException> { server.getPersonalChatWith("user", userRegistered.id) }
        } else
            assert(false)
    }

    @Test
    fun `sending message with invalid token results in an exception`() {
        val server = Server()

        val authResA = server.register(UserCredentials("A", "aaaaa"))
        val tokenA = if (authResA is AuthSuccessful) authResA.token else null
        server.register(UserCredentials("B", "bbbbb"))

        if (tokenA != null) {
            val chat = server.getPersonalChatWith(tokenA, server.searchByUsername(tokenA, "B")!!.id)

            assertFailsWith<InvalidTokenException> { server.sendTextMessage("C", "_", chat.id) }
        } else {
            assert(false)
        }
    }

    @Test
    fun `getting messages from personal chat with invalid token results in an exception`() {
        val server = Server()

        val authResA = server.register(UserCredentials("A", "aaaaa"))
        val tokenA = if (authResA is AuthSuccessful) authResA.token else null
        server.register(UserCredentials("B", "bbbbb"))

        if (tokenA != null) {
            val chat = server.getPersonalChatWith(tokenA, server.searchByUsername(tokenA, "B")!!.id)

            assertFailsWith<InvalidTokenException> { server.getChatMessages("C", chat.id) }
        } else
            assert(false)
    }

    @Test
    fun `creating group chat with invalid token results in an exception`() {
        val server = Server()

        val authResA = server.register(UserCredentials("A", "aaaaa"))
        val tokenA = if (authResA is AuthSuccessful) authResA.token else null
        val authResB = server.register(UserCredentials("B", "bbbbb"))
        val tokenB = if (authResB is AuthSuccessful) authResB.token else null

        if (tokenA != null && tokenB != null) {
            val userA = server.searchByUsername(tokenA, "A")!!
            val userB = server.searchByUsername(tokenB, "B")!!

            val members = listOf(userA.id, userB.id)

            assertFailsWith<InvalidTokenException> { server.createGroupChat("C", "", members) }
        } else
            assert(false)
    }
}