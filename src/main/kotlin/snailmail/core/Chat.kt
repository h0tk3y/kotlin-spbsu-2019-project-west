package snailmail.core

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.reflect.KClass

@TypeFor(field = "type", adapter = ChatAdapter::class)
abstract class Chat(val type: String, val id: UUID) {
    abstract fun hasMember(user: UUID): Boolean
}

class ChatAdapter : TypeAdapter<Chat> {
    override fun classFor(type: Any): KClass<out Chat> = when (type as String) {
        "personal" -> PersonalChat::class
        "group" -> GroupChat::class
        else -> throw IllegalArgumentException("Illegal chat type: $type")
    }
}