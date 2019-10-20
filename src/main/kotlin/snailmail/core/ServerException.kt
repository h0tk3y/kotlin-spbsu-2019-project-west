package snailmail.core

import com.beust.klaxon.*
import snailmail.core.api.APITransportMapping
import kotlin.reflect.KClass

@TypeFor(field = APITransportMapping.Convention.errorType, adapter = ServerExceptionAdapter::class)
abstract class ServerException(message: String) : Exception(message) {
    abstract fun errorType(): String
}

class InvalidTokenException : ServerException("Invalid Token") {
    override fun errorType() = "invalid token"
}

class InvalidChatId : ServerException("Invalid chat id") {
    override fun errorType() = "invalid chat id"
}

class UserIsNotMemberException : ServerException("User is not a member of this chat") {
    override fun errorType() = "user is not a member"
}

class ProtocolErrorException(message: String = "Request is malformed") : ServerException(message) {
    override fun errorType() = "protocol error"
}

class InternalServerErrorException(message: String = "Something bad happened...") : ServerException(message) {
    override fun errorType() = "internal error"
}

class ServerExceptionAdapter : TypeAdapter<ServerException> {
    override fun classFor(type: Any): KClass<out ServerException> = when (type as String) {
        InvalidTokenException().errorType() -> InvalidTokenException::class
        InvalidChatId().errorType() -> InvalidChatId::class
        UserIsNotMemberException().errorType() -> UserIsNotMemberException::class
        ProtocolErrorException().errorType() -> ProtocolErrorException::class
        InternalServerErrorException().errorType() -> InternalServerErrorException::class
        else -> throw IllegalArgumentException("Unknown error-type: $type")
    }
}

class ServerExceptionConverter : Converter {
    override fun canConvert(cls: Class<*>) =
            arrayOf(InvalidTokenException::class.java,
                    InvalidChatId::class.java,
                    UserIsNotMemberException::class.java,
                    ProtocolErrorException::class.java,
                    InternalServerErrorException::class.java).contains(cls)

    override fun fromJson(jv: JsonValue): Any? {
        if (jv.obj == null) throw java.lang.IllegalArgumentException("Malformed Server Exception")
        val errorType = (jv.obj?.get(APITransportMapping.Convention.errorType) as? String)
                ?: throw java.lang.IllegalArgumentException("Malformed Server Exception")
        val message = (jv.obj?.get("message") as? String)
                ?: throw java.lang.IllegalArgumentException("Malformed Server Exception")
        return when (errorType) {
            InvalidTokenException().errorType() -> InvalidTokenException()
            InvalidChatId().errorType() -> InvalidChatId()
            UserIsNotMemberException().errorType() -> UserIsNotMemberException()
            ProtocolErrorException().errorType() -> ProtocolErrorException(message)
            InternalServerErrorException().errorType() -> InternalServerErrorException(message)
            else -> throw IllegalArgumentException("Unknown error-type: $errorType")
        }
    }

    override fun toJson(value: Any): String {
        val e = value as ServerException
        return """{"${APITransportMapping.Convention.errorType}": "${e.errorType()}", "message": "${e.message?.apply { Render.escapeString(this) }}"}"""
    }
}
