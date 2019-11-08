@file:Suppress("EXPERIMENTAL_API_USAGE")

package snailmail.core

import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import java.util.*
import kotlin.reflect.KClass

@TypeFor(field = "type", adapter = MediaAdapter::class)
sealed class Media(val type: String)

class Photo(
        val photo: UUID,
        val thumbnail: UUID,
        val size: ULong
) : Media("photo")

class File(
        val file: UUID,
        val size: ULong,
        val filename: String
) : Media("file")

class MediaAdapter : TypeAdapter<Media> {
    override fun classFor(type: Any): KClass<out Media> = when (type as String) {
        "photo" -> Photo::class
        "file" -> File::class
        else -> throw IllegalArgumentException("Unknown media type: $type")
    }
}