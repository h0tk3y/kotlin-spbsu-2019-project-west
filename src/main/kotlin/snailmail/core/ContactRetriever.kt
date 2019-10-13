package snailmail.core

import java.util.*

interface ContactRetriever {
    fun getContact(viewerUser: UUID, targetUser: UUID): Contact
}