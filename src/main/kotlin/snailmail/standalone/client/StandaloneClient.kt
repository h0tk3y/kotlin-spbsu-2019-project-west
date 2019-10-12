package snailmail.standalone.client

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import snailmail.client.ConsoleClient
import snailmail.client.transport.WebsocketClient

fun main() {
    val api = WebsocketClient("127.0.0.1", 9999)
    val consoleClient = ConsoleClient(api)
    GlobalScope.launch {
        api.run()
    }

    consoleClient.startSession()
    while (consoleClient.writeCommand());
    consoleClient.endSession()
}