package snailmail.standalone.client

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import snailmail.client.lanterna.LanternaClient
import snailmail.client.transport.WebsocketClient

fun main(args: Array<String>) {
    val host = if (args.size == 0) {
        "127.0.0.1"
    } else {
        args[0]
    }
    var port = 9999
    if (args.size >= 2) {
        try {
            port = args[1].toInt()
        } catch (e: Exception) {
            println(e)
            return
        }
    }
    val api = WebsocketClient(host, port)
    GlobalScope.launch {
        api.run()
    }

    LanternaClient(api).run()
}