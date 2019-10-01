
package snailmail

import snailmail.core.*
import java.util.*

class DemoMessageRetriever : MessageRetriever {
    override fun getMessages(chat: UUID): List<Message> = listOf(
            TextMessage(UUID.randomUUID(), UUID.randomUUID(), Date(), seen = true, content = "Hello, world")
    )
}

fun main(args: Array<String>) {
    val messageRetriever = DemoMessageRetriever()
    val messages = messageRetriever.getMessages(UUID.randomUUID())
    for (message in messages) {
        print(message)
    }
}
