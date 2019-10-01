package snailmail.core

interface UserAuthenticator {
    fun checkPassword(username: String, password: String): Boolean
    fun registerUser(username: String, password: String)
}