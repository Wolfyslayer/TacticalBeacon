package com.tacticalbeacon.pins

import com.tacticalbeacon.data.model.Pin
import com.tacticalbeacon.data.model.PinCategory
import com.tacticalbeacon.data.model.PinColor
import com.tacticalbeacon.data.repository.PinRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinManager @Inject constructor(
    private val pinRepository: PinRepository
) {

    private val _pins = MutableStateFlow<List<Pin>>(emptyList())
    val pins: StateFlow<List<Pin>> = _pins.asStateFlow()

    private val _selectedPin = MutableStateFlow<Pin?>(null)
    val selectedPin: StateFlow<Pin?> = _selectedPin.asStateFlow()

    fun getAllPins(): StateFlow<List<Pin>> = _pins

    fun getPinById(id: String): Pin? = _pins.value.find { it.id == id }

    fun savePin(pin: Pin) {
        _pins.value = _pins.value.filter { it.id != pin.id } + pin
    }

    fun updatePin(pin: Pin) {
        _pins.value = _pins.value.map { if (it.id == pin.id) pin else it }
    }

    fun deletePin(pin: Pin) {
        _pins.value = _pins.value.filter { it.id != pin.id }
        if (_selectedPin.value?.id == pin.id) {
            _selectedPin.value = null
        }
    }

    fun selectPin(pin: Pin) {
        _selectedPin.value = pin
    }

    fun deselectPin() {
        _selectedPin.value = null
    }

    fun getPinsByCategory(category: PinCategory): List<Pin> {
        return _pins.value.filter { it.category == category }
    }

    fun getPinsByColor(color: PinColor): List<Pin> {
        return _pins.value.filter { it.color == color }
    }

    fun searchPins(query: String): List<Pin> {
        if (query.isBlank()) return _pins.value
        return _pins.value.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.notes.contains(query, ignoreCase = true) ||
            it.icon.label.contains(query, ignoreCase = true)
        }
    }
}