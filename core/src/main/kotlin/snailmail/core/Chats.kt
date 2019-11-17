package snailmail.core

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
sealed class Chat(val type: String, val id: UUID) {
    abstract fun hasMember(user: UUID): Boolean
    /*private companion object {
        @JsonCreator
        @JvmStatic
        fun findBySimpleName(simpleName: String): Chat? {
            return Chat::class.sealedSubclasses.first {
                it.simpleName == simpleName
            }.objectInstance
        }
    }*/
}

class PersonalChat(
        id: UUID,
        val person1: UUID,
        val person2: UUID
) : Chat("personal", id) {
    override fun hasMember(user: UUID): Boolean {
        return person1 == user || person2 == user
    }

    fun notMe(user: UUID): UUID {
        if (user == person1) return person2
        return person1
    }
}

class GroupChat(id: UUID,
                val title: String,
                val owner: UUID,
                val members: List<UUID>,
                val avatar: Photo? = null,
                val blacklist: List<UUID> = listOf(),
                val publicTag: String? = null,
                val privateInviteToken: String? = null
) : Chat("group", id) {
    override fun hasMember(user: UUID): Boolean {
        return owner == user || members.contains(user)
    }
}