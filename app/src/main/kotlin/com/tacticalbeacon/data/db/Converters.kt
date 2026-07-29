package com.tacticalbeacon.data.db

import androidx.room.TypeConverter
import com.tacticalbeacon.data.model.PinColor
import com.tacticalbeacon.data.model.PinIcon

class Converters {

    @TypeConverter
    fun fromPinIcon(icon: PinIcon): String = icon.name

    @TypeConverter
    fun toPinIcon(value: String): PinIcon =
        PinIcon.entries.firstOrNull { it.name == value } ?: PinIcon.WAYPOINT

    @TypeConverter
    fun fromPinColor(color: PinColor): String = color.name

    @TypeConverter
    fun toPinColor(value: String): PinColor =
        PinColor.entries.firstOrNull { it.name == value } ?: PinColor.OLIVE
}
