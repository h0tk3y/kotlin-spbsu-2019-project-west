package core.models

import java.util.*

abstract class Message(val ID: UUID, val authorID: UUID, val recipientID: UUID, val text: String) {
}