package snailmail.server.data.media

import snailmail.core.Media
import java.io.InputStream
import java.io.OutputStream

class DisabledMediaStorage : MediaStorage {
    override fun exists(media: Media): Boolean = throw Exception("Media Storage is disabled")
    override fun delete(media: Media) = throw Exception("Media Storage is disabled")
    override fun reader(media: Media): InputStream = throw Exception("Media Storage is disabled")
    override fun writer(media: Media): OutputStream = throw Exception("Media Storage is disabled")
}