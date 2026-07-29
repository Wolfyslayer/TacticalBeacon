package com.tacticalbeacon.data.db

import androidx.room.*
import com.tacticalbeacon.data.model.Breadcrumb
import kotlinx.coroutines.flow.Flow

@Dao
interface BreadcrumbDao {

    @Query("SELECT * FROM breadcrumbs WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getBreadcrumbsForSession(sessionId: String): Flow<List<Breadcrumb>>

    @Query("SELECT * FROM breadcrumbs WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getBreadcrumbsForSessionSync(sessionId: String): List<Breadcrumb>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreadcrumb(breadcrumb: Breadcrumb)

    @Query("DELETE FROM breadcrumbs WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM breadcrumbs")
    suspend fun deleteAll()

    @Query("SELECT DISTINCT sessionId FROM breadcrumbs ORDER BY timestamp DESC")
    suspend fun getAllSessionIds(): List<String>

    // Keep only the last N breadcrumbs per session to avoid excessive storage
    @Query("""
        DELETE FROM breadcrumbs WHERE id NOT IN (
            SELECT id FROM breadcrumbs WHERE sessionId = :sessionId 
            ORDER BY timestamp DESC LIMIT :keepCount
        ) AND sessionId = :sessionId
    """)
    suspend fun pruneSession(sessionId: String, keepCount: Int = 2000)
}
