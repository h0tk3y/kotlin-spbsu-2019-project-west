package snailmail.core

import java.util.*

data class GroupChatPreferences(
        val owner: UUID,
        val targetChat: UUID,
        val title: String?
)