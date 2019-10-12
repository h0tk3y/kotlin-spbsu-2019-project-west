package snailmail.core

import java.util.*

class GroupChat(id: UUID, val owner: UUID, val members: List<UUID>) : Chat("group", id) {
    override fun hasMember(user: UUID): Boolean {
        return owner == user || members.contains(user)
    }
}