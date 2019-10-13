package snailmail.core.api

import snailmail.core.User
import java.util.*

interface UserAPI {
    fun searchByUsername(token: AuthToken, username: String): User?
    fun getUserById(token: AuthToken, id: UUID): User?
}