package snailmail.server

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import snailmail.server.transport.RestHttpServer

class ServerArgs(parser: ArgParser) {
    val secret by parser.storing("Server secret key").default("secret")
    val port by parser.storing("Server port") { toInt() } .default(9999)
}

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::ServerArgs).run {
        RestHttpServer(Server(secret), secret).run(port)
    }
}