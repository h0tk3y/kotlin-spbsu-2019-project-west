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
class UserDoesNotExistException : ServerException("User does not exist")
class UserIsBannedException : ServerException("User is banned")
class ChatDoesNotExistOrUnavailableException : ServerException("Chat does not exist or unavailable")
class UserIsNotMemberException : ServerException("User is not a member of this chat")
class UserIsAlreadyMemberException : ServerException("User is already a member of this chat")
class PublicTagIsUnavailableException : ServerException("Public tag is unavailable")
class MediaDoesNotExistException : ServerException("Media does not exist")
class MessageDoesNotExistException : ServerException("Message does not exist")
class OperationFailedException(message: String = "Operation failed") : ServerException(message)
class ProtocolErrorException(message: String = "Request is malformed") : ServerException(message)
class InternalServerErrorException(message: String = "Something bad happened...") : ServerException(message)