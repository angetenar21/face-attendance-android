package com.salarybox.app.ui.staff

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.salarybox.app.data.local.entity.StaffEntity
import com.salarybox.app.data.repository.StaffRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StaffHomeViewModel(
    staffRepository: StaffRepository
) : ViewModel() {
    val staffList: StateFlow<List<StaffEntity>> = staffRepository.getAllStaff()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedStaff = MutableStateFlow<StaffEntity?>(null)
    val selectedStaff: StateFlow<StaffEntity?> = _selectedStaff

    fun selectStaff(staff: StaffEntity) {
        _selectedStaff.value = staff
    }
}

class StaffHomeViewModelFactory(
    private val staffRepository: StaffRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StaffHomeViewModel(staffRepository) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffHomeScreen(
    viewModel: StaffHomeViewModel,
    onNavigateToMarkAttendance: (Long) -> Unit,
    onNavigateToAttendanceHistory: (Long) -> Unit
) {
    val staffList by viewModel.staffList.collectAsStateWithLifecycle()
    val selectedStaff by viewModel.selectedStaff.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }

    // Auto-select the first staff member if none is selected
    LaunchedEffect(staffList) {
        if (selectedStaff == null && staffList.isNotEmpty()) {
            viewModel.selectStaff(staffList.first())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Staff Dashboard") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to the Staff Portal",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Select a staff member below to simulate their login. This selector is used for demo purposes to easily test different users without repeatedly logging in and out.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Staff Selector Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedStaff?.name ?: "No staff available",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Simulate Logged-In User") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    staffList.forEach { staff ->
                        DropdownMenuItem(
                            text = { Text("${staff.name} (${staff.employeeId})") },
                            onClick = {
                                viewModel.selectStaff(staff)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            val isEnrolled = selectedStaff?.faceEmbedding != null

            Button(
                onClick = {
                    selectedStaff?.let { onNavigateToMarkAttendance(it.id) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = selectedStaff != null && isEnrolled
            ) {
                Text("Mark Attendance")
            }
            
            if (selectedStaff != null && !isEnrolled) {
                Text(
                    text = "Face not enrolled. Contact admin to enrol your face before marking attendance.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    selectedStaff?.let { onNavigateToAttendanceHistory(it.id) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = selectedStaff != null
            ) {
                Text("View My Attendance")
            }
        }
    }
}
