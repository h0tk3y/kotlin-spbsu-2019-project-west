package snailmail.core

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import java.util.*
import kotlin.reflect.KClass

@TypeFor(field = "type", adapter = MessageAdapter::class)
abstract class Message(
        val type: String,
        val id: UUID,
        val chatId: UUID,
        val sender: UUID? = null,
        val date: Date,
        val seen: Boolean = false
)


class MessageAdapter : TypeAdapter<Message> {
    override fun classFor(type: Any): KClass<out Message> = when (type as String) {
        "text" -> TextMessage::class
        "service" -> ServiceMessage::class
        else -> throw IllegalArgumentException("Unknown message type: $type")
    }
}