package com.tacticalbeacon.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tacticalbeacon.data.model.Breadcrumb
import com.tacticalbeacon.data.model.Pin

@Database(
    entities = [Pin::class, Breadcrumb::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pinDao(): PinDao
    abstract fun breadcrumbDao(): BreadcrumbDao

    companion object {
        const val DATABASE_NAME = "tactical_beacon.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pins ADD COLUMN description TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE pins ADD COLUMN category TEXT NOT NULL DEFAULT 'NAVIGATION'")
                db.execSQL("ALTER TABLE pins ADD COLUMN status TEXT NOT NULL DEFAULT 'ACTIVE'")
                db.execSQL("ALTER TABLE pins ADD COLUMN priority TEXT NOT NULL DEFAULT 'NORMAL'")
                db.execSQL("ALTER TABLE pins ADD COLUMN creator TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE pins ADD COLUMN timeCreated INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE pins ADD COLUMN timeModified INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE pins SET timeCreated = createdAt WHERE createdAt IS NOT NULL")
                db.execSQL("UPDATE pins SET timeModified = updatedAt WHERE updatedAt IS NOT NULL")
                db.execSQL("ALTER TABLE pins DROP COLUMN createdAt")
                db.execSQL("ALTER TABLE pins DROP COLUMN updatedAt")
            }
        }

        fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addMigration(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
