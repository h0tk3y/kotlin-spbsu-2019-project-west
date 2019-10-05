package snailmail.core

interface UserAuthenticator {
    fun checkPassword(credentials: UserCredentials): Boolean
    fun registerUser(credentials: UserCredentials)
}