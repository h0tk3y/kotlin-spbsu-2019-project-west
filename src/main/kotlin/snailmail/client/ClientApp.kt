package snailmail.client

import snailmail.server.Server

fun main() {
    val server = Server()
    val consoleClient = ConsoleClient(server)
    consoleClient.run()
}