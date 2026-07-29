package com.tacticalbeacon.team

import javax.inject.Singleton

@Singleton
class SharingManager {

    interface ShareCallback {
        fun onSuccess(message: String)
        fun onError(message: String)
    }

    fun shareLocation(memberId: String, latitude: Double, longitude: Double) {
        // Placeholder for future encrypted sync implementation
    }

    fun sharePin(pinId: String, memberId: String) {
        // Placeholder for future encrypted sync implementation
    }

    fun syncTeamState() {
        // Placeholder for future encrypted sync implementation
    }
}