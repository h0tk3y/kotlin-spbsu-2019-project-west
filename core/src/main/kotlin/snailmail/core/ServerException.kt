package snailmail.core

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