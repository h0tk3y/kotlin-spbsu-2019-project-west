package core.api

interface AuthAPI {
    fun authorize(credentials: Credentials): AuthorizationResult
}