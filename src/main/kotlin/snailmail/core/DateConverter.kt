package snailmail.core

import com.beust.klaxon.*
import java.util.*

class DateConverter : Converter {
    override fun canConvert(cls: Class<*>) = cls == Date::class.java

    override fun fromJson(jv: JsonValue): Any? {
        return Date(jv.longValue ?: return null)
    }

    override fun toJson(value: Any): String = (value as Date).time.toString()
}
