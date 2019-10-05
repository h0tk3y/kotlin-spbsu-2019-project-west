package snailmail.core.api

typealias AuthToken = String

sealed class AuthenticationResult {
    abstract val successful: Boolean
}

class AuthSuccessful(val token: AuthToken) : AuthenticationResult() {
    override val successful: Boolean
        get() = true
}

class AuthWrongCredentials : AuthenticationResult() {
    override val successful: Boolean
        get() = false
}

class AuthRegisterFailed(val message: String) : AuthenticationResult() {
    override val successful: Boolean
        get() = false
}

class AuthError : AuthenticationResult() {
    override val successful: Boolean
        get() = false
}