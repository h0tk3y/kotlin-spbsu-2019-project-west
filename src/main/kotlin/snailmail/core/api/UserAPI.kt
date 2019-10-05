package snailmail.core.api

import snailmail.core.User

interface UserAPI {
    fun searchByUsername(username: String): User?
}