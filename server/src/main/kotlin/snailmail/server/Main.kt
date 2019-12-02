package snailmail.server

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import org.jetbrains.exposed.sql.Database
import snailmail.server.data.DataBase
import snailmail.server.data.LocalDataBase
import snailmail.server.data.MySQL
import snailmail.server.data.media.FileSystemMediaStorage
import snailmail.server.transport.RestHttpServer
import java.io.File

class ServerArgs(parser: ArgParser) {
    val secret by parser.storing("Server secret key").default("secret")
    val port by parser.storing("Server port") { toInt() } .default(9999)
    val dbType by parser.storing("Database to use (In-Memory|MySQL)").default("In-Memory")
    val mediaPath by parser.storing("Path for user media storage").default(".snailmail/media/")
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

        val mediaStorageFile = File(mediaPath)
        if (!mediaStorageFile.exists()) mediaStorageFile.mkdirs()

        RestHttpServer(Server(secret, db, FileSystemMediaStorage(mediaStorageFile)), secret).run(port)
    }
}