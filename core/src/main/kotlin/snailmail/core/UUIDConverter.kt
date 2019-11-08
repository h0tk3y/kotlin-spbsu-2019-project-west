package snailmail.core

import com.beust.klaxon.*
import java.util.*

class UUIDConverter : Converter {
    override fun canConvert(cls: Class<*>) = cls == UUID::class.java

    override fun fromJson(jv: JsonValue): Any? {
        return UUID.fromString(jv.string ?: return null)
    }

    override fun toJson(value: Any): String = "\"${value as UUID}\""
}