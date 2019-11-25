package snailmail


import org.junit.jupiter.api.Assertions.*
import snailmail.core.*
import snailmail.server.Server
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import java.util.*

internal class ServerTest {
    private fun generateServer(block: (Server, AuthToken, AuthToken, AuthToken, AuthToken, GroupChat, PersonalChat) -> Unit) {
        generateServerWithFourUsers { server, tokenA, tokenB, tokenC, tokenD ->
            val chat = server.getPersonalChatWith(tokenA, server.getUserByUsername(tokenA,"B").id)
            val members = listOf("C", "D").map {
                server.getUserByUsername(tokenA, it).id
            }
            val gChat = server.createGroupChat(tokenB, "chat", members)
            with(server) {
                sendTextMessage(tokenB, "1", gChat.id)
                sendTextMessage(tokenC, "2", gChat.id)
                sendTextMessage(tokenD, "3", gChat.id)
                sendTextMessage(tokenA, "4", chat.id)
                sendTextMessage(tokenB, "5", chat.id)
            }
            block(server, tokenA, tokenB, tokenC, tokenD, gChat, chat)
        }
    }

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
        assertThrows<UnavailableUsernameException> { server.register(UserCredentials("user", "password")) }
    }

    @Test
    fun `successful reg and auth`() {
        val server = Server()
        val userCredentials = UserCredentials("user", "abacaba")

        assert(server.register(userCredentials).isNotEmpty())
        assert(server.authenticate(userCredentials).isNotEmpty())
    }

    @Test
    fun `reg with empty password or username` () {
        val server = Server()

        assertThrows<ProtocolErrorException> { server.register(UserCredentials("", "q")) }
        assertThrows<ProtocolErrorException> { server.register(UserCredentials("a", "")) }
    }

    @Test
    fun `auth with wrong password`() {
        val server = Server()
        val userCredentials = UserCredentials("user", "abacaba")

        assert(server.register(userCredentials).isNotEmpty())
        assertThrows<WrongCredentialsException> { server.authenticate(UserCredentials("user", "password")) }
    }

    @Test
    fun `unregistered user tries to auth`() {
        val server = Server()

        assertThrows<WrongCredentialsException> { server.authenticate(UserCredentials("user", "password")) }
    }

    @Test
    fun `auth with empty password or username` () {
        val server = Server()

        assertThrows<ProtocolErrorException> { server.authenticate(UserCredentials("", "q")) }
        assertThrows<ProtocolErrorException> { server.authenticate(UserCredentials("a", "")) }
    }

    @Test
    fun `successful change credentials` () {
        val server = Server()

        val token1 = server.register(UserCredentials("a", "1"))
        val token2 = server.changeCredentials(token1, UserCredentials("a", "2"))
        assertDoesNotThrow { server.getChats(token2)}
        assertThrows<InvalidTokenException> { server.getChats(token1) }
        assertEquals(token2, server.authenticate(UserCredentials("a", "2")))
    }

    @Test
    fun `new username in change credentials` () {
        val server = Server()

        val token1 = server.register(UserCredentials("a", "1"))
        assertThrows<ProtocolErrorException> { server.changeCredentials(token1, UserCredentials("b", "2"))}
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
    fun `get chat by id` () {
        generateServerWithTwoUsers { server, tokenA, _ ->
            val chat = server.getPersonalChatWith(tokenA,
                    server.getUserByUsername(tokenA, "B").id)
            assertEquals(chat, server.getChatById(tokenA, chat.id))
        }
    }

    @Test
    fun `get nonexistent chat` () {
        generateServerWithTwoUsers { server, tokenA, _ ->
            val chat = server.getPersonalChatWith(tokenA,
                    server.getUserByUsername(tokenA, "B").id)
            var newId = UUID.randomUUID()
            while (newId == chat.id) newId = UUID.randomUUID()
            assertThrows<InvalidTokenException> { server.getChatById(tokenA, newId) }
        }
    }

    @Test
    fun `personal chat with wrong user id` () {
        val server = Server()
        val token = server.register(UserCredentials("user", "abacaba"))
        val id = server.getUserByUsername(token, "user").id
        var idThatNotExist = UUID.randomUUID()
        while (idThatNotExist == id) idThatNotExist = UUID.randomUUID()
        assertThrows<UserDoesNotExistException> { server.getPersonalChatWith(token, idThatNotExist) }
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

        assertThrows<UserDoesNotExistException> { server.getUserByUsername(token, "alice") }
    }

    @Test
    fun `sending a message with wrong chat id` () {
        generateServerWithTwoUsers { server, tokenA, _ ->
            assertThrows<ChatDoesNotExistOrUnavailableException> { server.sendTextMessage(tokenA, "<3", UUID.randomUUID()) }
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
    fun `sending an empty message` () {
        generateServerWithTwoUsers { server, tokenA, _ ->
            val chat = server.getPersonalChatWith(tokenA, server.getUserByUsername(tokenA, "B").id)
            assertThrows<ProtocolErrorException> { server.sendTextMessage(tokenA, "", chat.id) }
        }
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
            val groupChat = server.createGroupChat(tokenA, "chat", members)
            assertEquals(server.getUserByUsername(tokenA, "A").id, groupChat.owner)
            assertEquals(members, groupChat.members)
            assertEquals("chat", groupChat.title)
        }
    }

    @Test
    fun `creating group chat with empty title results in an exception` () {
        generateServerWithTwoUsers { server, tokenA, _ ->
            assertThrows<ProtocolErrorException> { server.createGroupChat(tokenA, "", listOf(server.getUserByUsername(tokenA, "B").id)) }
        }
    }

    @Test
    fun `creating group chat with owner in list of members results in an exception` () {
        generateServerWithTwoUsers {server, tokenA, _ ->
            val members = listOf("A", "B").map {
                server.getUserByUsername(tokenA, it).id
            }
            assertThrows<ProtocolErrorException> { server.createGroupChat(tokenA, "chat", members) }
        }
    }

    @Test
    fun `creating group chat with duplicates in members results in an exception` () {
        generateServerWithTwoUsers {server, tokenA, _ ->
            val members = listOf("B", "B").map {
                server.getUserByUsername(tokenA, it).id
            }
            assertThrows<ProtocolErrorException> { server.createGroupChat(tokenA, "chat", members) }
        }
    }

    @Test
    fun `creating group chat with nonexistent user in members results in an exception` () {
        generateServerWithTwoUsers { server, tokenA, _ ->
            assertThrows<ProtocolErrorException> { server.createGroupChat(tokenA, "chat", listOf(server.getUserByUsername(tokenA, "B").id, UUID.randomUUID())) }
        }
    }

    @Test
    fun `successful joining group chat with public tag` () {
        generateServer { server, tokenA, tokenB, _, _, gChat, _ ->
            server.updatePublicTagOfGroupChat(tokenB, gChat.id, "cc")
            assertDoesNotThrow { server.joinGroupChatUsingPublicTag(tokenA, "cc") }
        }
    }

    @Test
    fun `joining group chat with invalid public tag results in an exception` () {
        generateServer { server, tokenA, tokenB, _, _, gChat, _ ->
            server.updatePublicTagOfGroupChat(tokenB, gChat.id, "cc")
            assertThrows<ChatDoesNotExistOrUnavailableException> { server.joinGroupChatUsingPublicTag(tokenA, "ncc") }
        }
    }

    @Test
    fun `joinGroupChatUsingPublicTag returns updated chat` () {
        generateServer { server, tokenA, tokenB, _, _, gChat, _ ->
            server.updatePublicTagOfGroupChat(tokenB, gChat.id, "cc")
            val newChat = server.joinGroupChatUsingPublicTag(tokenA, "cc")
            assertEquals(newChat.members, gChat.members.plus(server.getUserByUsername(tokenB, "A")))
        }
    }

    @Test
    fun `joining group chat with public tag results in an exception if user in the blacklist` () {
        generateServer { server, _, tokenB, tokenC, _, gChat, _ ->
            server.updatePublicTagOfGroupChat(tokenB, gChat.id, "cc")
            val newChat = server.kickUserFromGroupChat(tokenB, gChat.id, server.getUserByUsername(tokenB,"C").id)
            assertThrows<ChatDoesNotExistOrUnavailableException> { server.joinGroupChatUsingPublicTag(tokenC, "cc") }
        }
    }

    @Test
    fun `kicked user not in chat members` () {
        generateServer { server, _, tokenB, tokenC, _, gChat, _ ->
            val newChat = server.kickUserFromGroupChat(tokenB, gChat.id, server.getUserByUsername(tokenB,"C").id)
            assert(!newChat.members.contains(server.getUserByUsername(tokenB, "C").id))
        }
    }

    @Test
    fun `kicked user in the blacklist` () {
        generateServer { server, _, tokenB, tokenC, _, gChat, _ ->
            val newChat = server.kickUserFromGroupChat(tokenB, gChat.id, server.getUserByUsername(tokenB,"C").id)
            assert(newChat.blacklist.contains(server.getUserByUsername(tokenB, "C").id))
        }
    }

    @Test
    fun `kicking not a member of this chat results in an exception` () {
        generateServer { server, _, tokenB, tokenC, _, gChat, _ ->
            assertThrows<UserIsNotMemberException> {  server.kickUserFromGroupChat(tokenB, gChat.id, server.getUserByUsername(tokenB,"A").id) }
        }
    }

    @Test
    fun `invited user in the member list` () {
        generateServer { server, _, tokenB, tokenC, _, gChat, _ ->
            val newChat = server.inviteUserToGroupChat(tokenB, gChat.id, server.getUserByUsername(tokenB,"A").id)
            assert(newChat.members.contains(server.getUserByUsername(tokenB,"A").id))
        }
    }

    @Test
    fun `successful invitation of user from blacklist to group chat` () {
        generateServer { server, _, tokenB, tokenC, _, gChat, _ ->
            val newChat = server.kickUserFromGroupChat(tokenB, gChat.id, server.getUserByUsername(tokenB,"C").id)
            assertDoesNotThrow { server.inviteUserToGroupChat(tokenB, newChat.id, server.getUserByUsername(tokenB,"C").id) }
        }
    }

    @Test
    fun `can't invite chat member to chat` () {
        generateServer { server, _, tokenB, _, _, gChat, _ ->
            assertThrows<UserIsAlreadyMemberException> { server.inviteUserToGroupChat(tokenB, gChat.id, server.getUserByUsername(tokenB,"C").id) }
        }
    }

    @Test
    fun `invitation to nonexistent chat results in an exception` () {
        generateServer { server, _, tokenB, _, _, gChat, _ ->
            assertThrows<ChatDoesNotExistOrUnavailableException> { server.inviteUserToGroupChat(tokenB,
                    UUID.randomUUID(),
                    server.getUserByUsername(tokenB,"C").id) }
        }
    }

    @Test
    fun `removing user from the blacklist results in an exception if user is not in the blacklist` () {
        generateServer { server, _, tokenB, _, _, gChat, _ ->
            assertThrows<ProtocolErrorException> { server.removeUserFromBlacklistOfGroupChat(tokenB, gChat.id, server.getUserByUsername(tokenB,"A").id) }
        }
    }

    @Test
    fun `removeUserFromBlacklistOfGroupChat - user no more in blacklist` () {
        generateServer { server, _, tokenB, tokenC, _, gChat, _ ->
            val newChat = server.kickUserFromGroupChat(tokenB, gChat.id, server.getUserByUsername(tokenB,"C").id)
            val newNewChat = server.removeUserFromBlacklistOfGroupChat(tokenB, newChat.id, server.getUserByUsername(tokenB,"C").id)
            assert(!newNewChat.blacklist.contains(server.getUserByUsername(tokenB, "C").id))
        }
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

        assertThrows<UserIsNotMemberException> { server.getChatMessages(tokenE, chat.id) }
    }

    @Test
    fun `getting chats with invalid token results in an exception`() {
        val server = Server()
        assertThrows<InvalidTokenException> { server.getChats("user") }
    }

    @Test
    fun `searching by username with invalid token results in an exception`() {
        val server = Server()
        server.register(UserCredentials("userRegistered", "abacaba"))

        assertThrows<InvalidTokenException> { server.getUserByUsername("user", "userRegistered") }
    }

    @Test
    fun `getting personal chat with invalid token results in an exception`() {
        val server = Server()

        val token = server.register(UserCredentials("userRegistered", "abacaba"))
        val userRegistered = server.getUserByUsername(token, "userRegistered")

        assertThrows<InvalidTokenException> { server.getPersonalChatWith("user", userRegistered.id) }
    }

    @Test
    fun `getting chat by id with invalid token results in an exception` () {
        generateServerWithTwoUsers { server, tokenA, _ ->
            val chat = server.getPersonalChatWith(tokenA,
                    server.getUserByUsername(tokenA, "B").id)
            assertThrows<InvalidTokenException> { server.getChatById("kek", chat.id) }
        }
    }

    @Test
    fun `sending message with invalid token results in an exception`() {
        generateServerWithTwoUsers { server, tokenA, _ ->
            val chat = server.getPersonalChatWith(tokenA, server.getUserByUsername(tokenA, "B").id)
            assertThrows<InvalidTokenException> { server.sendTextMessage("C", "_", chat.id) }
        }
    }

    @Test
    fun `getting messages from personal chat with invalid token results in an exception`() {
        generateServerWithTwoUsers { server, tokenA, _ ->
            val chat = server.getPersonalChatWith(tokenA, server.getUserByUsername(tokenA, "B").id)
            assertThrows<InvalidTokenException> { server.getChatMessages("C", chat.id) }
        }
    }

    @Test
    fun `creating group chat with invalid token results in an exception`() {
        generateServerWithTwoUsers { server, tokenA, tokenB ->
            val userA = server.getUserByUsername(tokenA, "A")
            val userB = server.getUserByUsername(tokenB, "B")
            val members = listOf(userA.id, userB.id)
            assertThrows<InvalidTokenException> { server.createGroupChat("C", "", members) }
        }
    }

    @Test
    fun `changing credentials with invalid token results in an exception` () {
        val server = Server()

        server.register(UserCredentials("a", "1"))
        assertThrows<InvalidTokenException> { server.changeCredentials("kek", UserCredentials("a", "2"))}
    }

    @Test
    fun `joining group chat with public tag with invalid token results in an exception` () {
        generateServer { server, _, tokenB, _, _, gChat, _ ->
            server.updatePublicTagOfGroupChat(tokenB, gChat.id, "cc")
            server.joinGroupChatUsingPublicTag("kek", "cc")
        }
    }

    @Test
    fun `kicking user with invalid token results in an exception` () {
        generateServer { server, _, tokenB, tokenC, _, gChat, _ ->
            assertThrows<InvalidTokenException> {  server.kickUserFromGroupChat("kek", gChat.id, server.getUserByUsername(tokenB,"A").id) }
        }
    }

    @Test
    fun `inviting user to group chat with invalid token results in an exception` () {
        generateServer { server, _, tokenB, _, _, gChat, _ ->
            assertThrows<InvalidTokenException> { server.inviteUserToGroupChat("kek", gChat.id, server.getUserByUsername(tokenB,"A").id) }
        }
    }

    @Test
    fun `removing user from the blacklist with invalid token results in an exception` () {
        generateServer { server, _, tokenB, _, _, gChat, _ ->
            val newChat = server.kickUserFromGroupChat(tokenB, gChat.id, server.getUserByUsername(tokenB,"C").id)
            assertThrows<InvalidTokenException> { server.removeUserFromBlacklistOfGroupChat("kek", newChat.id, server.getUserByUsername(tokenB,"C").id) }
        }
    }

    @Test
    fun `leaving chat with invalid token results in an exception` () {
        generateServer { server, _, _, _, _, gChat, _ ->
            assertThrows<InvalidTokenException> { server.leaveGroupChat("kek", gChat.id) }
        }
    }

    @Test
    fun `changing chat title with invalid token results in an exception` () {
        generateServer { server, _, _, _, _, gChat, _ ->
            assertThrows<InvalidTokenException> { server.changeTitleOfGroupChat("kek", gChat.id, "nt") }
        }
    }
}