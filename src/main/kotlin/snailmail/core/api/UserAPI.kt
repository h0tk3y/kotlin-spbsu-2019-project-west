package snailmail.core.api

import snailmail.core.User

interface UserAPI {
    fun searchByUsername(token: AuthToken, username: String): User?
}