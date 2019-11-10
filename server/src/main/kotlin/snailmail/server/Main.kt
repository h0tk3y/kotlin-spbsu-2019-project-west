package snailmail.server

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import snailmail.server.transport.HttpServer

class ServerArgs(parser: ArgParser) {
    val port by parser.storing("Server port") { toInt() } .default(9999)
}

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::ServerArgs).run {
        HttpServer(Server()).run(port)
    }
}