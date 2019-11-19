@file:Suppress("EXPERIMENTAL_API_USAGE")

package snailmail.core

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
sealed class Media(val type: String) {
    /*private companion object {
        @JsonCreator
        @JvmStatic
        fun findBySimpleName(simpleName: String): Media? {
            return Media::class.sealedSubclasses.first {
                it.simpleName == simpleName
            }.objectInstance
        }
    }*/
}

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