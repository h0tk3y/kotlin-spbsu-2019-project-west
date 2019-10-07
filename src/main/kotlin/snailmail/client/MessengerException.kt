package snailmail.client

open class MessengerException(message: String) : Exception(message)

class UserNotFoundException(message: String) : MessengerException(message)

class NoAuthTokenException(message: String) : MessengerException(message)