package snailmail.server.data.media

import snailmail.core.File
import snailmail.core.Media
import snailmail.core.MediaDoesNotExistException
import snailmail.core.Photo
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class FileSystemMediaStorage(private val storageDir: java.io.File) : MediaStorage {
    init {
        require(storageDir.isDirectory) { "FileSystem storage: storageDir parameter must be a directory" }
        require(storageDir.canRead()) { "FileSystem storage: can't read files! (permission is missing?)" }
        require(storageDir.canWrite()) { "FileSystem storage: can't write files! (permission is missing?)" }
    }

    override fun exists(media: Media): Boolean = media.toFile().exists()

    override fun delete(media: Media) {
        val file = media.toFile()
        if (!file.exists()) throw MediaDoesNotExistException()
        file.delete()
    }

    override fun reader(media: Media): InputStream {
        val file = media.toFile()
        if (!file.exists()) throw MediaDoesNotExistException()
        return file.inputStream()
    }

    @ExperimentalUnsignedTypes
    override fun writer(media: Media): OutputStream {
        val file = media.toFile()
        file.createNewFile()
        if (media.size > file.usableSpace.toULong())
            throw StorageOutOfSpaceException()
        return file.outputStream()
    }

    private fun Media.toFile(): java.io.File = java.io.File(storageDir, id.toString())
}