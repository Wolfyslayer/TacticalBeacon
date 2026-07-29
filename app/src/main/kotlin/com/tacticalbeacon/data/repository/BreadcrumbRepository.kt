package com.tacticalbeacon.data.repository

import com.tacticalbeacon.data.db.BreadcrumbDao
import com.tacticalbeacon.data.model.Breadcrumb
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BreadcrumbRepository @Inject constructor(
    private val breadcrumbDao: BreadcrumbDao
) {
    fun getBreadcrumbsForSession(sessionId: String): Flow<List<Breadcrumb>> =
        breadcrumbDao.getBreadcrumbsForSession(sessionId)

    suspend fun addBreadcrumb(breadcrumb: Breadcrumb) {
        breadcrumbDao.insertBreadcrumb(breadcrumb)
        // Prune to keep memory usage bounded
        breadcrumbDao.pruneSession(breadcrumb.sessionId, 2000)
    }

    suspend fun clearSession(sessionId: String) = breadcrumbDao.deleteSession(sessionId)

    suspend fun clearAll() = breadcrumbDao.deleteAll()
}
