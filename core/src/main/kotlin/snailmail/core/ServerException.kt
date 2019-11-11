package snailmail.core

import com.fasterxml.jackson.annotation.JsonCreator

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
class InvalidChatIdException : ServerException("Invalid chat id")
class UserIsNotMemberException : ServerException("User is not a member of this chat")
class ProtocolErrorException(message: String = "Request is malformed") : ServerException(message)
class InternalServerErrorException(message: String = "Internal error") : ServerException(message)