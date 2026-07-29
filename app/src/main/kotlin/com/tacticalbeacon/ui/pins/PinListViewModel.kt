package com.tacticalbeacon.ui.pins

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tacticalbeacon.data.model.Pin
import com.tacticalbeacon.data.repository.PinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinListViewModel @Inject constructor(
    private val pinRepository: PinRepository
) : ViewModel() {

    val pins = pinRepository.getAllPins()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _operationResult = MutableSharedFlow<String>()
    val operationResult = _operationResult.asSharedFlow()

    fun importJson(content: String) {
        viewModelScope.launch {
            pinRepository.importFromJson(content).fold(
                onSuccess = { count -> _operationResult.emit("Imported $count pins") },
                onFailure = { e -> _operationResult.emit("Import failed: ${e.message}") }
            )
        }
    }

    fun importGpx(content: String) {
        viewModelScope.launch {
            pinRepository.importFromGpx(content).fold(
                onSuccess = { count -> _operationResult.emit("Imported $count pins") },
                onFailure = { e -> _operationResult.emit("Import failed: ${e.message}") }
            )
        }
    }

    suspend fun exportJson(pins: List<Pin>): String = pinRepository.exportToJson(pins)

    fun exportGpx(pins: List<Pin>): String = pinRepository.exportToGpx(pins)
}
