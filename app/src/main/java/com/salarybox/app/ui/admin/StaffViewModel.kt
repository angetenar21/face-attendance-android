package com.salarybox.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.salarybox.app.data.local.entity.StaffEntity
import com.salarybox.app.data.repository.StaffRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// UI state models
// ---------------------------------------------------------------------------

/** Drives the [StaffListScreen] content area. */
sealed interface StaffListUiState {
    /** Initial value — upstream Flow hasn't emitted yet. */
    data object Loading : StaffListUiState

    /** The table is empty; show the empty-state prompt. */
    data object Empty : StaffListUiState

    /** At least one staff member exists. */
    data class Success(val staff: List<StaffEntity>) : StaffListUiState
}

/** Drives the [AddStaffScreen] form. */
data class AddStaffUiState(
    val name: String = "",
    val employeeId: String = "",
    val nameError: String? = null,
    val employeeIdError: String? = null,
    val isSaving: Boolean = false
)

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

/**
 * Shared ViewModel scoped to the admin nested nav-graph.
 *
 * Because both [StaffListScreen] and [AddStaffScreen] acquire this ViewModel
 * from the same backstack entry (the parent graph route), they share a single
 * instance.  This means:
 *  - The staff list Flow updates automatically the moment AddStaff saves.
 *  - The uniqueness check in AddStaff reads from the exact same repository.
 *  - Navigation events are modelled as a [Channel] (one-shot) so they are
 *    never re-delivered after a recomposition.
 */
class StaffViewModel(private val staffRepository: StaffRepository) : ViewModel() {

    // --- Staff list --------------------------------------------------------

    /**
     * Transforms the repository Flow into a sealed [StaffListUiState].
     *
     * [SharingStarted.WhileSubscribed(5_000)] keeps the upstream alive for
     * 5 s after the last collector leaves — avoids restarting the DB query
     * on every configuration change while allowing clean-up after genuine
     * navigation away.
     */
    val staffListUiState: StateFlow<StaffListUiState> = staffRepository
        .getAllStaff()
        .map { list ->
            if (list.isEmpty()) StaffListUiState.Empty
            else StaffListUiState.Success(list)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StaffListUiState.Loading
        )

    // --- Add-staff form ----------------------------------------------------

    private val _addStaffState = MutableStateFlow(AddStaffUiState())
    val addStaffState: StateFlow<AddStaffUiState> = _addStaffState.asStateFlow()

    /**
     * One-shot navigation event: emitted once after a successful save.
     * Using a [Channel] (capacity = 1) guarantees the event is delivered
     * exactly once even if the collector briefly disappears during recomposition.
     */
    private val _navigateBackEvent = Channel<Unit>(capacity = Channel.BUFFERED)
    val navigateBackEvent = _navigateBackEvent.receiveAsFlow()

    fun onNameChange(name: String) {
        _addStaffState.update { it.copy(name = name, nameError = null) }
    }

    fun onEmployeeIdChange(id: String) {
        _addStaffState.update { it.copy(employeeId = id, employeeIdError = null) }
    }

    /**
     * Validates the form, checks uniqueness, and inserts the new staff member.
     *
     * On success: emits [navigateBackEvent].
     * On failure: sets inline error messages on the relevant fields.
     */
    fun saveStaff() {
        val state = _addStaffState.value
        val name = state.name.trim()
        val employeeId = state.employeeId.trim()

        // --- Synchronous validation ---
        var nameError: String? = null
        var employeeIdError: String? = null

        if (name.isBlank()) nameError = "Name cannot be empty"
        if (employeeId.isBlank()) employeeIdError = "Employee ID cannot be empty"

        if (nameError != null || employeeIdError != null) {
            _addStaffState.update {
                it.copy(nameError = nameError, employeeIdError = employeeIdError)
            }
            return
        }

        // --- Async: uniqueness check + insert ---
        viewModelScope.launch {
            _addStaffState.update { it.copy(isSaving = true) }

            val exists = staffRepository.getByEmployeeId(employeeId) != null
            if (exists) {
                _addStaffState.update {
                    it.copy(
                        isSaving = false,
                        employeeIdError = "Employee ID '$employeeId' is already registered"
                    )
                }
                return@launch
            }

            staffRepository.addStaff(
                StaffEntity(name = name, employeeId = employeeId)
            )

            _addStaffState.update { it.copy(isSaving = false) }
            resetForm()
            _navigateBackEvent.send(Unit)
        }
    }

    /** Clears the form — call when the AddStaff screen is closed or re-opened. */
    fun resetForm() {
        _addStaffState.value = AddStaffUiState()
    }
}

// ---------------------------------------------------------------------------
// Factory (no Hilt — manual DI)
// ---------------------------------------------------------------------------

class StaffViewModelFactory(
    private val staffRepository: StaffRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StaffViewModel::class.java)) {
            return StaffViewModel(staffRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
