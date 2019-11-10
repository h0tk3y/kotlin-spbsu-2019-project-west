package snailmail.core

import com.beust.klaxon.*
import snailmail.core.api.ApiTransportMapping
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor

@TypeFor(field = ApiTransportMapping.Convention.errorType, adapter = ServerExceptionAdapter::class)
sealed class ServerException(message: String) : Exception(message) {
    abstract fun errorType(): String
}

class UnavailableUsernameException : ServerException("Unavailable username") {
    override fun errorType() = "unavailable username"
}

class WrongCredentialsException : ServerException("Wrong credentials") {
    override fun errorType() = "wrong credentials"
}

class InvalidTokenException : ServerException("Invalid token") {
    override fun errorType() = "invalid token"
}

class UserDoesNotExistException : ServerException("User does not exist") {
    override fun errorType() = "user does not exist"
}

class UserIsBannedException : ServerException("User is banned") {
    override fun errorType(): String = "user is banned"
}

class ChatDoesNotExistOrUnavailableException : ServerException("Chat does not exist or unavailable") {
    override fun errorType() = "chat does not exist or unavailable"
}

class UserIsNotMemberException : ServerException("User is not a member of this chat") {
    override fun errorType() = "user is not a member"
}

class UserIsAlreadyMemberException : ServerException("User is already a member of this chat") {
    override fun errorType() = "user is already a member"
}

class PublicTagIsUnavailableException : ServerException("Public tag is unavailable") {
    override fun errorType() = "public tag is unavailable"
}

class MediaDoesNotExistException : ServerException("Media does not exist") {
    override fun errorType() = "media does not exist"
}

class MessageDoesNotExistException : ServerException("Message does not exist") {
    override fun errorType() = "message does not exist"
}

class OperationFailedException(message: String = "Operation failed") : ServerException(message) {
    override fun errorType() = "operation failed"
}

class ProtocolErrorException(message: String = "Request is malformed") : ServerException(message) {
    override fun errorType() = "protocol error"
}

class InternalServerErrorException(message: String = "Something bad happened...") : ServerException(message) {
    override fun errorType() = "internal error"
}

class ServerExceptionAdapter : TypeAdapter<ServerException> {
    private val mapping = ServerException::class.sealedSubclasses.associateBy { it.createInstance().errorType() }
    override fun classFor(type: Any): KClass<out ServerException> {
        return mapping[type as String] ?: throw IllegalArgumentException("Unknown error-type: $type")
    }
}

class ServerExceptionConverter : Converter {
    private val mapping = ServerException::class.sealedSubclasses.associateBy { it.createInstance().errorType() }

    override fun canConvert(cls: Class<*>) = ServerException::class.sealedSubclasses.map { it.java }.contains(cls)

    override fun fromJson(jv: JsonValue): Any? {
        if (jv.obj == null) throw java.lang.IllegalArgumentException("Malformed Server Exception")
        val errorType = (jv.obj?.get(ApiTransportMapping.Convention.errorType) as? String)
                ?: throw java.lang.IllegalArgumentException("Malformed Server Exception")
        val message = (jv.obj?.get("message") as? String)
                ?: throw java.lang.IllegalArgumentException("Malformed Server Exception")
        val klass = mapping[errorType]
                ?: throw IllegalArgumentException("Unknown error-type: ${errorType}")
        val hasMessage = klass.primaryConstructor?.parameters?.any { it.name?.contentEquals("message") ?: false }
                ?: false
        if (hasMessage)
            return klass.primaryConstructor?.call(message)
        return klass.createInstance()
    }

    override fun toJson(value: Any): String {
        val e = value as ServerException
        return """{"${ApiTransportMapping.Convention.errorType}": "${e.errorType()}", "message": "${e.message?.apply { Render.escapeString(this) }}"}"""
    }
}
