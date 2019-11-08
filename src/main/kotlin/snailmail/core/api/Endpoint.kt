package snailmail.core.api

@Target(AnnotationTarget.FUNCTION)
annotation class Endpoint(
        val uri: String,
        val method: String
)