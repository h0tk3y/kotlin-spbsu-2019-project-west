package snailmail.core

import com.beust.klaxon.*
import snailmail.core.api.ApiTransportMapping
import kotlin.reflect.KClass

@TypeFor(field = ApiTransportMapping.Convention.errorType, adapter = ServerExceptionAdapter::class)
abstract class ServerException(message: String) : Exception(message) {
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

class InvalidChatIdException : ServerException("Invalid chat id") {
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
        UnavailableUsernameException().errorType() -> UnavailableUsernameException::class
        WrongCredentialsException().errorType() -> WrongCredentialsException::class
        InvalidTokenException().errorType() -> InvalidTokenException::class
        InvalidChatIdException().errorType() -> InvalidChatIdException::class
        UserIsNotMemberException().errorType() -> UserIsNotMemberException::class
        ProtocolErrorException().errorType() -> ProtocolErrorException::class
        InternalServerErrorException().errorType() -> InternalServerErrorException::class
        else -> throw IllegalArgumentException("Unknown error-type: $type")
    }
}

class ServerExceptionConverter : Converter {
    override fun canConvert(cls: Class<*>) =
            arrayOf(UnavailableUsernameException::class.java,
                    WrongCredentialsException::class.java,
                    InvalidTokenException::class.java,
                    InvalidChatIdException::class.java,
                    UserIsNotMemberException::class.java,
                    ProtocolErrorException::class.java,
                    InternalServerErrorException::class.java).contains(cls)

    override fun fromJson(jv: JsonValue): Any? {
        if (jv.obj == null) throw java.lang.IllegalArgumentException("Malformed Server Exception")
        val errorType = (jv.obj?.get(ApiTransportMapping.Convention.errorType) as? String)
                ?: throw java.lang.IllegalArgumentException("Malformed Server Exception")
        val message = (jv.obj?.get("message") as? String)
                ?: throw java.lang.IllegalArgumentException("Malformed Server Exception")
        return when (errorType) {
            UnavailableUsernameException().errorType() -> UnavailableUsernameException()
            WrongCredentialsException().errorType() -> WrongCredentialsException()
            InvalidTokenException().errorType() -> InvalidTokenException()
            InvalidChatIdException().errorType() -> InvalidChatIdException()
            UserIsNotMemberException().errorType() -> UserIsNotMemberException()
            ProtocolErrorException().errorType() -> ProtocolErrorException(message)
            InternalServerErrorException().errorType() -> InternalServerErrorException(message)
            else -> throw IllegalArgumentException("Unknown error-type: $errorType")
        }
    }

    override fun toJson(value: Any): String {
        val e = value as ServerException
        return """{"${ApiTransportMapping.Convention.errorType}": "${e.errorType()}", "message": "${e.message?.apply { Render.escapeString(this) }}"}"""
    }
}
