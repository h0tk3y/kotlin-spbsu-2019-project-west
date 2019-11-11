package snailmail.core

import com.fasterxml.jackson.annotation.JsonCreator
import java.util.*
import kotlin.reflect.KClass

sealed class Message(
        val type: String,
        val id: UUID,
        val chat: UUID,
        val date: Date
) {
    private companion object {
        @JsonCreator
        @JvmStatic
        fun findBySimpleName(simpleName: String): Message? {
            return Message::class.sealedSubclasses.first {
                it.simpleName == simpleName
            }.objectInstance
        }
    }
}

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
        val caption: String? = null
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