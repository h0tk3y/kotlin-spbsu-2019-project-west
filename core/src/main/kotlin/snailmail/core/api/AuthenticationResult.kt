package snailmail.core.api

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import snailmail.core.ServerRequestAdapter
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass

typealias AuthToken = String

@TypeFor(field = "status", adapter = AuthenticationResultAdapter::class)
sealed class AuthenticationResult(val status: String) {
    abstract val successful: Boolean
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


class AuthenticationResultAdapter: TypeAdapter<AuthenticationResult> {
    override fun classFor(type: Any): KClass<out AuthenticationResult> = when (type as String) {
        "successful" -> AuthSuccessful::class
        "wrongCredentials" -> AuthWrongCredentials::class
        "registerFailed" -> AuthRegisterFailed::class
        "error" -> AuthError::class
        else -> throw IllegalArgumentException("Invalid AuthenticationResult status: $type")
    }
}