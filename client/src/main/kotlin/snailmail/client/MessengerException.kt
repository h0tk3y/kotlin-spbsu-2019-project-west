package snailmail.client

open class MessengerException(message: String) : Exception(message)

class UserNotFoundException(message: String) : MessengerException(message)

class NotAuthenticatedException(message: String) : MessengerException(message)

class ChatNotFoundException(message: String) : MessengerException(message)
