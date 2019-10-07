package snailmail.client

open class MessangerException(message : String) : Exception(message)

class UserNotFoundException(message: String) : MessangerException(message)

class NullTokenException(message: String) : MessangerException(message)