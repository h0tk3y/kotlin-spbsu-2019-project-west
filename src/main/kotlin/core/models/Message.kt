package core.models

import java.util.*

abstract class Message(val authorID: UUID, val recipientID: UUID, val text: String) {
    val ID: UUID? = TODO()
}