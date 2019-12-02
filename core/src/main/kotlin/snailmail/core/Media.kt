@file:Suppress("EXPERIMENTAL_API_USAGE")

package snailmail.core

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
sealed class Media(val type: String, val id: UUID, val size: ULong) {
}

/**
 * Automatically converted to JPEG on server side
 */

class Photo(
        id: UUID,
        size: ULong,
        val thumbnail: UUID
) : Media("photo", id, size)

class File(
        id: UUID,
        size: ULong,
        val filename: String
) : Media("file", id, size)