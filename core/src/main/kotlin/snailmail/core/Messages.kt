package snailmail.core

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import java.util.*
import kotlin.reflect.KClass

@TypeFor(field = "type", adapter = MessageAdapter::class)
sealed class Message(
        val type: String,
        val id: UUID,
        val chat: UUID,
        val date: Date
)

class TextMessage(
        id: UUID,
        chat: UUID,
        date: Date,
        val sender: UUID,
        val content: String,
        val edited: Boolean = false
) : Message("text", id, chat, date)

class MediaMessage(
        id: UUID,
        chat: UUID,
        date: Date,
        val sender: UUID,
        val media: Media,
        val caption: String? = null,
        val edited: Boolean = false
) : Message("media", id, chat, date)

class DeletedMessage(
        id: UUID,
        chat: UUID,
        date: Date,
        val sender: UUID
) : Message("deleted", id, chat, date)

sealed class ServiceMessage(
        type: String,
        id: UUID,
        chat: UUID,
        date: Date
) : Message(type, id, chat, date)

class UserJoinedMessage(
        id: UUID,
        chat: UUID,
        date: Date,
        val user: UUID
) : ServiceMessage("service.userJoined", id, chat, date)

class UserJoinedByInvitationalTokenMessage(
        id: UUID,
        chat: UUID,
        date: Date,
        val user: UUID,
        val token: String
) : ServiceMessage("service.userJoinedByToken", id, chat, date)

class UserInvitedMessage(
        id: UUID,
        chat: UUID,
        date: Date,
        val causer: UUID,
        val invitedUser: UUID
) : ServiceMessage("service.userInvited", id, chat, date)

class UserLeftMessage(
        id: UUID,
        chat: UUID,
        date: Date,
        val user: UUID
) : ServiceMessage("service.userLeft", id, chat, date)

class TitleChangedMessage(
        id: UUID,
        chat: UUID,
        date: Date,
        val causer: UUID,
        val newTitle: String
) : ServiceMessage("service.titleChanged", id, chat, date)

class GroupChatCreatedMessage(
        id: UUID,
        chat: UUID,
        date: Date
) : ServiceMessage("service.groupChatCreated", id, chat, date)

class MessageAdapter : TypeAdapter<Message> {
    override fun classFor(type: Any): KClass<out Message> = when (type as String) {
        "text" -> TextMessage::class
        "media" -> MediaMessage::class
        "deleted" -> DeletedMessage::class
        "service.userJoined" -> UserJoinedMessage::class
        "service.userJoinedByToken" -> UserJoinedByInvitationalTokenMessage::class
        "service.userInvited" -> UserInvitedMessage::class
        "service.userLeft" -> UserLeftMessage::class
        "service.titleChanged" -> TitleChangedMessage::class
        "service.groupChatCreated" -> GroupChatCreatedMessage::class
        else -> throw IllegalArgumentException("Unknown message type: $type")
    }
}