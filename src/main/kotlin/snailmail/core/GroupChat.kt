package snailmail.core

import java.util.*

class GroupChat(id: UUID, var title : String, val owner: UUID, val members: List<UUID>) : Chat(id) {
    override fun hasMember(user: UUID): Boolean {
        return owner == user || members.contains(user)
    }
}