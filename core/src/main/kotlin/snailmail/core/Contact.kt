package snailmail.core

import java.util.*

data class Contact(
    val owner: UUID,
    val targetUser: UUID,
    var displayName: String? = null,
    var banned: Boolean = false
)