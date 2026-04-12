package com.forlks.personal_wellness_routine.ui.screen.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forlks.personal_wellness_routine.data.repository.RoutineRepository
import com.forlks.personal_wellness_routine.data.repository.WellnessPointRepository
import com.forlks.personal_wellness_routine.domain.model.Routine
import com.forlks.personal_wellness_routine.domain.model.RoutineCategory
import com.forlks.personal_wellness_routine.domain.model.WpEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class RoutineUiState(
    val routines: List<Routine> = emptyList(),
    val selectedCategory: RoutineCategory = RoutineCategory.ALL
)

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val wellnessPointRepository: WellnessPointRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineUiState())
    val uiState: StateFlow<RoutineUiState> = _uiState.asStateFlow()

    init {
        observeRoutines(RoutineCategory.ALL)
    }

    private fun observeRoutines(category: RoutineCategory) {
        viewModelScope.launch {
            routineRepository.getRoutinesByCategory(category).collect { routines ->
                _uiState.update { it.copy(routines = routines) }
            }
        }
    }

    fun selectCategory(category: RoutineCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        observeRoutines(category)
    }

    fun toggleComplete(routineId: Long, date: String, isCompleted: Boolean) {
        viewModelScope.launch {
            routineRepository.toggleComplete(routineId, date, isCompleted)
            if (isCompleted) {
                wellnessPointRepository.earnPoints(
                    eventType = WpEvent.ROUTINE,
                    description = "루틴 완료"
                )
            }
        }
    }

    fun toggleCompleteToday(routineId: Long, isCompleted: Boolean) {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        toggleComplete(routineId, today, isCompleted)
    }

    fun addRoutine(routine: Routine) {
        viewModelScope.launch {
            routineRepository.addRoutine(routine)
        }
    }

    fun updateRoutine(routine: Routine) {
        viewModelScope.launch {
            routineRepository.updateRoutine(routine)
        }
    }

    fun deleteRoutine(id: Long) {
        viewModelScope.launch {
            routineRepository.deleteRoutine(id)
        }
    }
}
