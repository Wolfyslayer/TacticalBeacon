package com.tacticalbeacon.di

import android.content.Context
import com.google.gson.Gson
import com.tacticalbeacon.data.db.AppDatabase
import com.tacticalbeacon.data.db.BreadcrumbDao
import com.tacticalbeacon.data.db.PinDao
import com.tacticalbeacon.data.repository.PinRepository
import com.tacticalbeacon.gps.AccuracyManager
import com.tacticalbeacon.gps.GpsManager
import com.tacticalbeacon.gps.HeadingManager
import com.tacticalbeacon.navigation.NavigationEngine
import com.tacticalbeacon.overlays.CompassOverlay
import com.tacticalbeacon.overlays.GridOverlay
import com.tacticalbeacon.overlays.MeasurementOverlay
import com.tacticalbeacon.overlays.RangeRingOverlay
import com.tacticalbeacon.pins.PinManager
import com.tacticalbeacon.pins.PinRenderer
import com.tacticalbeacon.tiles.TileCacheManager
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
        return AppDatabase.buildDatabase(context)
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

    @Provides
    @Singleton
    fun provideTileCacheManager(@ApplicationContext context: Context): TileCacheManager {
        return TileCacheManager(context)
    }

    @Provides
    @Singleton
    fun providePinManager(pinRepository: PinRepository): PinManager {
        return PinManager(pinRepository)
    }

    @Provides
    @Singleton
    fun providePinRenderer(): PinRenderer {
        return PinRenderer()
    }

    @Provides
    @Singleton
    fun provideNavigationEngine(
        @ApplicationContext context: Context,
        locationManager: com.tacticalbeacon.location.LocationManager
    ): NavigationEngine {
        return NavigationEngine(locationManager)
    }

    @Provides
    @Singleton
    fun provideGpsManager(@ApplicationContext context: Context): GpsManager {
        return GpsManager(context)
    }

    @Provides
    @Singleton
    fun provideHeadingManager(@ApplicationContext context: Context): HeadingManager {
        return HeadingManager(context)
    }

    @Provides
    @Singleton
    fun provideAccuracyManager(@ApplicationContext context: Context): AccuracyManager {
        return AccuracyManager(context)
    }
}
