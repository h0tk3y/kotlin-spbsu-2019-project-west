package snailmail.core

interface UserRetriever {
    fun getUsers(): List<User>
}