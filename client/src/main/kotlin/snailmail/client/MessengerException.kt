package snailmail.client

open class MessengerException(message: String) : Exception(message)

class NotAuthenticatedException(message: String = "You must authenticate first") : MessengerException(message)

class ChatNotFoundException(message: String = "Chat not found") : MessengerException(message)
