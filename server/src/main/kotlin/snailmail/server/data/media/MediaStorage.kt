package snailmail.server.data.media

import snailmail.core.Media
import snailmail.core.ServerException
import java.io.InputStream
import java.io.OutputStream

interface MediaStorage {
    /**
     * Return true if the instance of [media] exists and valid
     * and false otherwise
     */
    fun exists(media: Media): Boolean

    /**
     * Deletes the instance of [media] from storage
     * After that operation exists([media]) must return false
     * @throws MediaDoesNotExistException if instance doesn't exist
     */
    fun delete(media: Media)

    /**
     * @throws MediaDoesNotExistException
     * // might throw IO exception but I don't wanna bother with that rn
     */
    fun reader(media: Media): InputStream

    /**
     * @throws StorageOutOfSpaceException
     * // might throw IO exception but I don't wanna bother with that rn
     */
    fun writer(media: Media): OutputStream
}

class StorageOutOfSpaceException : Exception("Media storage is out of space!")