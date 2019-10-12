package snailmail.client

import snailmail.server.Server

fun main() {
    val server = Server()
    val consoleClient = ConsoleClient(server)
    consoleClient.startSession()
    while (consoleClient.writeCommand());
    consoleClient.endSession()
}