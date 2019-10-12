package snailmail.standalone.server

import snailmail.server.Server
import snailmail.server.transport.WebsocketServer

fun main() {
    WebsocketServer(Server()).run()
}