package com.tacticalbeacon.pins

import com.tacticalbeacon.R
import com.tacticalbeacon.data.model.Pin
import com.tacticalbeacon.data.model.PinCategory
import com.tacticalbeacon.data.model.PinColor
import com.tacticalbeacon.data.model.PinIcon

object PinIcons {

    fun getIconResource(icon: PinIcon): Int = when (icon) {
        PinIcon.CAMP -> R.drawable.ic_pin_camp
        PinIcon.VEHICLE -> R.drawable.ic_pin_vehicle
        PinIcon.CACHE -> R.drawable.ic_pin_cache
        PinIcon.HUNTING_STAND -> R.drawable.ic_pin_hunting
        PinIcon.WAYPOINT -> R.drawable.ic_pin_waypoint
        PinIcon.DANGER -> R.drawable.ic_pin_danger
        PinIcon.OBJECTIVE -> R.drawable.ic_pin_objective
        PinIcon.EXTRACTION -> R.drawable.ic_pin_extraction
        PinIcon.MEDICAL -> R.drawable.ic_pin_medical
        PinIcon.WATER -> R.drawable.ic_pin_water
        PinIcon.FOOD -> R.drawable.ic_pin_food
        PinIcon.OBSERVATION -> R.drawable.ic_pin_observation
    }

    fun getIconLabel(icon: PinIcon): String = icon.label

    fun getCategory(icon: PinIcon): PinCategory = when (icon) {
        PinIcon.CAMP, PinIcon.VEHICLE, PinIcon.CACHE, PinIcon.FOOD -> PinCategory.SUPPLY
        PinIcon.MEDICAL, PinIcon.WATER -> PinCategory.SAFETY
        PinIcon.DANGER -> PinCategory.HAZARD
        PinIcon.OBJECTIVE, PinIcon.EXTRACTION -> PinCategory.OBJECTIVE
        PinIcon.OBSERVATION -> PinCategory.OBSERVATION
        PinIcon.HUNTING_STAND, PinIcon.WAYPOINT -> PinCategory.NAVIGATION
    }
}