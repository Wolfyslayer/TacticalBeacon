package com.tacticalbeacon.data.db

import androidx.room.TypeConverter
import com.tacticalbeacon.data.model.PinCategory
import com.tacticalbeacon.data.model.PinColor
import com.tacticalbeacon.data.model.PinIcon
import com.tacticalbeacon.data.model.PinPriority
import com.tacticalbeacon.data.model.PinStatus

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

    @TypeConverter
    fun fromPinCategory(category: PinCategory): String = category.name

    @TypeConverter
    fun toPinCategory(value: String): PinCategory =
        PinCategory.entries.firstOrNull { it.name == value } ?: PinCategory.NAVIGATION

    @TypeConverter
    fun fromPinStatus(status: PinStatus): String = status.name

    @TypeConverter
    fun toPinStatus(value: String): PinStatus =
        PinStatus.entries.firstOrNull { it.name == value } ?: PinStatus.ACTIVE

    @TypeConverter
    fun fromPinPriority(priority: PinPriority): String = priority.name

    @TypeConverter
    fun toPinPriority(value: String): PinPriority =
        PinPriority.entries.firstOrNull { it.name == value } ?: PinPriority.NORMAL
}
