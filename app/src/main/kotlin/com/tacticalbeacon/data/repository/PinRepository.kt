package com.tacticalbeacon.data.repository

import com.tacticalbeacon.data.db.PinDao
import com.tacticalbeacon.data.model.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinRepository @Inject constructor(
    private val pinDao: PinDao,
    private val gson: Gson
) {

    fun getAllPins(): Flow<List<Pin>> = pinDao.getAllPins()

    suspend fun getPinById(id: String): Pin? = pinDao.getPinById(id)

    suspend fun savePin(pin: Pin) = pinDao.insertPin(pin)

    suspend fun updatePin(pin: Pin) = pinDao.updatePin(pin.copy(timeModified = System.currentTimeMillis()))

    suspend fun deletePin(pin: Pin) = pinDao.deletePin(pin)

    suspend fun deletePinById(id: String) = pinDao.deletePinById(id)

    suspend fun deleteAllPins() = pinDao.deleteAllPins()

    // ─── JSON Export ──────────────────────────────────────────────────────────

    suspend fun exportToJson(pins: List<Pin>): String {
        val exportData = ExportData(
            pins = pins.map { pin ->
                ExportPin(
                    id = pin.id,
                    name = pin.name,
                    description = pin.description,
                    notes = pin.notes,
                    latitude = pin.latitude,
                    longitude = pin.longitude,
                    altitude = pin.altitude,
                    icon = pin.icon.name,
                    color = pin.color.name,
                    category = pin.category.name,
                    status = pin.status.name,
                    priority = pin.priority.name,
                    createdAt = pin.timeCreated,
                    timeModified = pin.timeModified
                )
            }
        )
        return GsonBuilder().setPrettyPrinting().create().toJson(exportData)
    }

    suspend fun importFromJson(json: String): Result<Int> {
        return try {
            val exportData = gson.fromJson(json, ExportData::class.java)
            val pins = exportData.pins.map { ep ->
                Pin(
                    id = ep.id,
                    name = ep.name,
                    description = ep.description,
                    notes = ep.notes,
                    latitude = ep.latitude,
                    longitude = ep.longitude,
                    altitude = ep.altitude,
                    icon = PinIcon.entries.firstOrNull { it.name == ep.icon } ?: PinIcon.WAYPOINT,
                    color = PinColor.entries.firstOrNull { it.name == ep.color } ?: PinColor.OLIVE,
                    category = PinCategory.entries.firstOrNull { it.name == ep.category } ?: PinCategory.NAVIGATION,
                    status = PinStatus.entries.firstOrNull { it.name == ep.status } ?: PinStatus.ACTIVE,
                    priority = PinPriority.entries.firstOrNull { it.name == ep.priority } ?: PinPriority.NORMAL,
                    timeCreated = ep.createdAt,
                    timeModified = ep.timeModified
                )
            }
            pinDao.insertPins(pins)
            Result.success(pins.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── GPX Export ───────────────────────────────────────────────────────────

    fun exportToGpx(pins: List<Pin>): String {
        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.appendLine("""<gpx version="1.1" creator="TacticalBeacon" xmlns="http://www.topografix.com/GPX/1/1">""")
        sb.appendLine("""  <metadata>""")
        sb.appendLine("""    <name>TacticalBeacon Export</name>""")
        sb.appendLine("""    <time>${java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }.format(java.util.Date())}</time>""")
        sb.appendLine("""  </metadata>""")

        for (pin in pins) {
            sb.appendLine("""  <wpt lat="${pin.latitude}" lon="${pin.longitude}">""")
            if (pin.altitude != 0.0) {
                sb.appendLine("""    <ele>${pin.altitude}</ele>""")
            }
            sb.appendLine("""    <name>${escapeXml(pin.name)}</name>""")
            if (pin.notes.isNotBlank()) {
                sb.appendLine("""    <desc>${escapeXml(pin.notes)}</desc>""")
            }
            sb.appendLine("""    <sym>${pin.icon.label}</sym>""")
            sb.appendLine("""    <type>${pin.icon.name}</type>""")
            sb.appendLine("""    <extensions>""")
            sb.appendLine("""      <tb:color xmlns:tb="com.tacticalbeacon">${pin.color.name}</tb:color>""")
            sb.appendLine("""      <tb:id xmlns:tb="com.tacticalbeacon">${pin.id}</tb:id>""")
            sb.appendLine("""    </extensions>""")
            sb.appendLine("""  </wpt>""")
        }

        sb.appendLine("""</gpx>""")
        return sb.toString()
    }

    suspend fun importFromGpx(gpxContent: String): Result<Int> {
        return try {
            val pins = mutableListOf<Pin>()
            val wptPattern = Regex("""<wpt\s+lat="([^"]+)"\s+lon="([^"]+)">(.*?)</wpt>""", RegexOption.DOT_MATCHES_ALL)
            val namePattern = Regex("""<name>(.*?)</name>""")
            val descPattern = Regex("""<desc>(.*?)</desc>""")
            val elePattern = Regex("""<ele>(.*?)</ele>""")
            val typePattern = Regex("""<type>(.*?)</type>""")
            val colorPattern = Regex("""<tb:color[^>]*>(.*?)</tb:color>""")
            val categoryPattern = Regex("""<tb:category[^>]*>(.*?)</tb:category>""")
            val statusPattern = Regex("""<tb:status[^>]*>(.*?)</tb:status>""")
            val priorityPattern = Regex("""<tb:priority[^>]*>(.*?)</tb:priority>""")

            for (match in wptPattern.findAll(gpxContent)) {
                val lat = match.groupValues[1].toDoubleOrNull() ?: continue
                val lon = match.groupValues[2].toDoubleOrNull() ?: continue
                val body = match.groupValues[3]

                val name = namePattern.find(body)?.groupValues?.get(1)?.let { unescapeXml(it) } ?: "Imported Pin"
                val notes = descPattern.find(body)?.groupValues?.get(1)?.let { unescapeXml(it) } ?: ""
                val ele = elePattern.find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                val typeStr = typePattern.find(body)?.groupValues?.get(1) ?: ""
                val colorStr = colorPattern.find(body)?.groupValues?.get(1) ?: ""
                val categoryStr = categoryPattern.find(body)?.groupValues?.get(1) ?: ""
                val statusStr = statusPattern.find(body)?.groupValues?.get(1) ?: ""
                val priorityStr = priorityPattern.find(body)?.groupValues?.get(1) ?: ""

                val icon = PinIcon.entries.firstOrNull { it.name == typeStr } ?: PinIcon.WAYPOINT
                val color = PinColor.entries.firstOrNull { it.name == colorStr } ?: PinColor.OLIVE
                val category = PinCategory.entries.firstOrNull { it.name == categoryStr } ?: PinCategory.NAVIGATION
                val status = PinStatus.entries.firstOrNull { it.name == statusStr } ?: PinStatus.ACTIVE
                val priority = PinPriority.entries.firstOrNull { it.name == priorityStr } ?: PinPriority.NORMAL

                pins.add(Pin(
                    name = name,
                    description = "",
                    notes = notes,
                    latitude = lat,
                    longitude = lon,
                    altitude = ele,
                    icon = icon,
                    color = color,
                    category = category,
                    status = status,
                    priority = priority
                ))
            }

            pinDao.insertPins(pins)
            Result.success(pins.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun escapeXml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    private fun unescapeXml(text: String): String = text
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
}
