package com.salarybox.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.salarybox.app.data.local.entity.StaffEntity
import com.salarybox.app.data.model.Role
import com.salarybox.app.data.repository.AuthRepository
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
 * Accepts both [staffRepository] and [authRepository] so that when a staff
 * member is created, a linked login account is also automatically created.
 *
 * Staff login credentials:
 *   - username  = employeeId  (e.g. "EMP-001")
 *   - password  = "1234"      (default)
 */
class StaffViewModel(
    private val staffRepository: StaffRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // --- Staff list --------------------------------------------------------

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

    private val _navigateBackEvent = Channel<Unit>(capacity = Channel.BUFFERED)
    val navigateBackEvent = _navigateBackEvent.receiveAsFlow()

    fun onNameChange(name: String) {
        _addStaffState.update { it.copy(name = name, nameError = null) }
    }

    fun onEmployeeIdChange(id: String) {
        _addStaffState.update { it.copy(employeeId = id, employeeIdError = null) }
    }

    /**
     * Validates the form, inserts the staff record, and auto-creates a linked user account.
     * The staff member can then log in with:  username=employeeId  password=1234
     */
    fun saveStaff() {
        val state = _addStaffState.value
        val name = state.name.trim()
        val employeeId = state.employeeId.trim()

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

            // 1. Insert staff record; get the auto-generated staffId
            val staffId = staffRepository.addStaff(
                StaffEntity(name = name, employeeId = employeeId)
            )

            // 2. Auto-create a linked user account (username = employeeId, password = "1234")
            if (!authRepository.userExists(employeeId)) {
                authRepository.createUser(
                    username = employeeId,
                    password = "1234",
                    role = Role.STAFF,
                    staffId = staffId
                )
            } else {
                // User already existed — just link it to this staff record
                authRepository.linkUserToStaff(employeeId, staffId)
            }

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
    private val staffRepository: StaffRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StaffViewModel::class.java)) {
            return StaffViewModel(staffRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
