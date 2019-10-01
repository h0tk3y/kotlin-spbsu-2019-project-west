package snailmail.core

import java.util.*

interface ContactUpdater {
    fun setDisplayName(viewerUser: UUID, targetUser: UUID, displayName: String)
    fun ban(viewerUser: UUID, targetUser: UUID)
    fun pardon(viewerUser: UUID, targetUser: UUID)
}