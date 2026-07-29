package com.tacticalbeacon.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.tacticalbeacon.data.db.AppDatabase
import com.tacticalbeacon.data.db.BreadcrumbDao
import com.tacticalbeacon.data.db.PinDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providePinDao(database: AppDatabase): PinDao = database.pinDao()

    @Provides
    @Singleton
    fun provideBreadcrumbDao(database: AppDatabase): BreadcrumbDao = database.breadcrumbDao()

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
}
