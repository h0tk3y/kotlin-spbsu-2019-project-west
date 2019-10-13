package snailmail.core.api

import snailmail.core.UserCredentials

interface AuthAPI {
    fun authenticate(credentials: UserCredentials): AuthenticationResult
    fun register(credentials: UserCredentials): AuthenticationResult
}