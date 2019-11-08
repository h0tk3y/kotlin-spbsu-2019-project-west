package snailmail.core

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import java.util.*
import kotlin.reflect.KClass

@TypeFor(field = "type", adapter = ChatAdapter::class)
sealed class Chat(val type: String, val id: UUID) {
    abstract fun hasMember(user: UUID): Boolean
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

/**
 * What happens when owner leaves the chat:
 * (*) if there is no more users is the group chat, it must be deleted
 * (*) otherwise the new owner is the first user in the members list
 *
 * Owner cannot be kicked
 */
class GroupChat(id: UUID,
                val title: String,
                val owner: UUID,
                val members: List<UUID>,
                val blacklist: List<UUID>,
                val publicTag: String?,
                val privateInviteToken: String?
) : Chat("group", id) {
    override fun hasMember(user: UUID): Boolean {
        return owner == user || members.contains(user)
    }
}

class ChatAdapter : TypeAdapter<Chat> {
    override fun classFor(type: Any): KClass<out Chat> = when (type as String) {
        "personal" -> PersonalChat::class
        "group" -> GroupChat::class
        else -> throw IllegalArgumentException("Illegal chat type: $type")
    }
}