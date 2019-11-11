package snailmail.core

import com.fasterxml.jackson.annotation.JsonCreator
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass

typealias AuthToken = String

sealed class AuthenticationResult(val status: String) {
    abstract val successful: Boolean
    private companion object {
        @JsonCreator
        @JvmStatic
        fun findBySimpleName(simpleName: String): AuthenticationResult? {
            return AuthenticationResult::class.sealedSubclasses.first {
                it.simpleName == simpleName
            }.objectInstance
        }
    }
}

class AuthSuccessful(val token: AuthToken) : AuthenticationResult("successful") {
    override val successful: Boolean
        get() = true
}

class AuthWrongCredentials : AuthenticationResult("wrongCredentials") {
    override val successful: Boolean
        get() = false
}

class AuthRegisterFailed(val message: String) : AuthenticationResult("registerFailed") {
    override val successful: Boolean
        get() = false
}

class AuthError : AuthenticationResult("error") {
    override val successful: Boolean
        get() = false
}