package snailmail.core

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import kotlin.reflect.full.primaryConstructor

@JsonSerialize(using = ServerExceptionSerializer::class)
@JsonDeserialize(using = ServerExceptionDeserializer::class)
sealed class ServerException(val error: String) : Exception(error) {
    private companion object {
        @JsonCreator
        @JvmStatic
        fun findBySimpleName(simpleName: String): ServerException? {
            return ServerException::class.sealedSubclasses.first {
                it.simpleName == simpleName
            }.objectInstance
        }
    }
}

class UnavailableUsernameException : ServerException("Unavailable username")
class WrongCredentialsException : ServerException("Wrong credentials")
class InvalidTokenException : ServerException("Invalid token")
class UserDoesNotExistException : ServerException("User does not exist")
class UserIsBannedException : ServerException("User is banned")
class ChatDoesNotExistOrUnavailableException : ServerException("Chat does not exist or unavailable")
class GroupChatPreferencesDoesNotExist : ServerException("Group chat preferences does not exist")
class UserIsNotMemberException : ServerException("User is not a member of this chat")
class UserIsNotOwnerException : ServerException("User is not an owner of this chat")
class UserIsNotSenderException : ServerException("This is service message or user isn't sender")
class UserIsAlreadyMemberException : ServerException("User is already a member of this chat")
class PublicTagIsUnavailableException : ServerException("Public tag is unavailable")
class InviteTokenIsInvalidException : ServerException("Invite token is invalid")
class MediaDoesNotExistException : ServerException("Media does not exist")
class MessageDoesNotExistException : ServerException("Message does not exist")
class OperationFailedException(message: String = "Operation failed") : ServerException(message)
class ProtocolErrorException(message: String = "Request is malformed") : ServerException(message)
class InternalServerErrorException(message: String = "Something bad happened...") : ServerException(message)
class ContactDoesNotExist : ServerException("Contact doesn't exist")

class ServerExceptionSerializer : JsonSerializer<ServerException>() {
    override fun serialize(value: ServerException?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        if (value == null || gen == null) return

        gen.writeStartObject()
        gen.writeStringField("error-type", value.javaClass.simpleName)
        gen.writeStringField("error", value.error)
        gen.writeEndObject()
    }
}

class NotAServerExceptionException : Exception("This is definitely not a server exception")

class ServerExceptionDeserializer : JsonDeserializer<ServerException>() {
    @Suppress("UNCHECKED_CAST")
    override fun deserialize(parser: JsonParser?, ctxt: DeserializationContext?): ServerException {
        if (parser == null) throw IllegalArgumentException()
        try {
            val codec = parser.codec
            val node: JsonNode = codec.readTree(parser)
            val errorType = node.get("error-type").asText()!!
            val error = node.get("error").asText()!!
            val klass = ServerException::class.sealedSubclasses.first {
                it.simpleName == errorType
            }
            val constructor = klass.primaryConstructor!!
            if (constructor.parameters.isNotEmpty())
                return (constructor as (String) -> ServerException)(error)
            return (constructor as () -> ServerException)()
        } catch (e: Exception) {
            throw NotAServerExceptionException()
        }
    }
}