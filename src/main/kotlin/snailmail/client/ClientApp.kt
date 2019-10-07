package snailmail.client

import snailmail.server.Server

fun main() {
    val server = Server()
    val consoleClient = ConsoleClient(server)
    consoleClient.startSession()
    var cmd = readLine()!!
    while (cmd.toLowerCase() != "quit") {
        consoleClient.doCommand(cmd)
        cmd = readLine()!!
    }
    consoleClient.endSession()
}