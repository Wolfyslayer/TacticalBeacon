package com.tacticalbeacon.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tacticalbeacon.data.model.Breadcrumb
import com.tacticalbeacon.data.model.Pin

@Database(
    entities = [Pin::class, Breadcrumb::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pinDao(): PinDao
    abstract fun breadcrumbDao(): BreadcrumbDao

    companion object {
        const val DATABASE_NAME = "tactical_beacon.db"
    }
}
