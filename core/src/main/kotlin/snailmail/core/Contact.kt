package snailmail.core

import java.util.*

data class Contact(
        val owner: UUID,
        val targetUser: UUID,
        val displayName: String?,
        val banned: Boolean
)