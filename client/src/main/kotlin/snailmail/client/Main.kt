package snailmail.client

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import snailmail.client.lanterna.LanternaClient
import snailmail.client.transport.RestHttpClient

class ClientArgs(parser: ArgParser) {
    val gui by parser.flagging("Use experimental GUI")
    val host by parser.storing("Server host").default("127.0.0.1")
    val port by parser.storing("Server port") { toInt() } .default(9999)
}

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::ClientArgs).run {
        val httpClient = RestHttpClient(host, port)
        if (gui) {
            LanternaClient(httpClient).run()
        } else {
            ConsoleClient(httpClient).run()
        }
    }
}