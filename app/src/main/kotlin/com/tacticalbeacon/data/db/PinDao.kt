package com.tacticalbeacon.data.db

import androidx.room.*
import com.tacticalbeacon.data.model.Pin
import com.tacticalbeacon.data.model.PinCategory
import com.tacticalbeacon.data.model.PinStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PinDao {

    @Query("SELECT * FROM pins ORDER BY createdAt DESC")
    fun getAllPins(): Flow<List<Pin>>

    @Query("SELECT * FROM pins WHERE id = :id")
    suspend fun getPinById(id: String): Pin?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPin(pin: Pin)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPins(pins: List<Pin>)

    @Update
    suspend fun updatePin(pin: Pin)

    @Delete
    suspend fun deletePin(pin: Pin)

    @Query("DELETE FROM pins WHERE id = :id")
    suspend fun deletePinById(id: String)

    @Query("DELETE FROM pins")
    suspend fun deleteAllPins()

    @Query("SELECT COUNT(*) FROM pins")
    suspend fun getPinCount(): Int

    @Query("SELECT * FROM pins WHERE category = :category")
    fun getPinsByCategory(category: PinCategory): Flow<List<Pin>>

    @Query("SELECT * FROM pins WHERE status = :status")
    fun getPinsByStatus(status: PinStatus): Flow<List<Pin>>
}
