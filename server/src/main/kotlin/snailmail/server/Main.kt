package snailmail.server

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import org.jetbrains.exposed.sql.Database
import snailmail.server.data.DataBase
import snailmail.server.data.LocalDataBase
import snailmail.server.data.MySQL
import snailmail.server.transport.RestHttpServer

class ServerArgs(parser: ArgParser) {
    val secret by parser.storing("Server secret key").default("secret")
    val port by parser.storing("Server port") { toInt() } .default(9999)
    val dbType by parser.storing("Database to use (In-Memory|MySQL)").default("In-Memory")
}

fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::ServerArgs).run {
        val db: DataBase
        if (dbType == "MySQL") {
            val url = "jdbc:h2:mem:test"
            Database.connect(url, driver = "org.h2.Driver")
            db = MySQL()
        } else if (dbType == "In-Memory") {
            db = LocalDataBase()
        } else {
            throw InvalidArgumentException("Invalid DB type! Expected MySQL or In-Memory")
        }

        RestHttpServer(Server(secret, db), secret).run(port)
    }
}