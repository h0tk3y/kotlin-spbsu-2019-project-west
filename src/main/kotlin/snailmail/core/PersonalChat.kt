package snailmail.core

import java.util.*

class PersonalChat(id: UUID, val person1: UUID, val person2: UUID): Chat(id) {
    override fun hasMember(user: UUID): Boolean {
        return person1 == user || person2 == user
    }
}