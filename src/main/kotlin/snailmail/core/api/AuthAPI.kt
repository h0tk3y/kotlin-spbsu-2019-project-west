package snailmail.core.api

import snailmail.core.UserCredentials

interface AuthAPI {
    fun authenticate(credentials: UserCredentials): AuthToken
    fun register(credentials: UserCredentials): AuthToken
}