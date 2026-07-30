package com.tacticalbeacon.team

data class TeamMember(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val role: String = "Member",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val bearing: Float = 0f,
    val speed: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val isOnline: Boolean = false
)

data class TeamBeacon(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val isActive: Boolean = false
)