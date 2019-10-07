package snailmail.client

open class MessengerException(message: String) : Exception(message)

class UserNotFoundException(message: String) : MessengerException(message)

class NullTokenException(message: String) : MessengerException(message)