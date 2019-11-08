package snailmail.core

import java.util.*

data class User(
        val id: UUID,
        val username: String,
        val displayName: String,
        val email: String? = null,
        val avatar: Photo? = null
)
