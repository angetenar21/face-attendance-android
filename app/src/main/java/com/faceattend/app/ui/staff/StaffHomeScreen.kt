package com.faceattend.app.ui.staff

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.faceattend.app.data.local.entity.StaffEntity
import com.faceattend.app.data.repository.StaffRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StaffHomeUiState(
    val staff: StaffEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class StaffHomeViewModel(
    private val staffId: Long,
    private val staffRepository: StaffRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffHomeUiState())
    val uiState: StateFlow<StaffHomeUiState> = _uiState.asStateFlow()

    init {
        loadStaff()
    }

    private fun loadStaff() {
        viewModelScope.launch {
            val staff = staffRepository.getStaffById(staffId)
            if (staff != null) {
                _uiState.value = StaffHomeUiState(staff = staff, isLoading = false)
            } else {
                _uiState.value = StaffHomeUiState(isLoading = false, error = "Your staff profile was not found. Contact admin.")
            }
        }
    }
}

class StaffHomeViewModelFactory(
    private val staffId: Long,
    private val staffRepository: StaffRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StaffHomeViewModel(staffId, staffRepository) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffHomeScreen(
    viewModel: StaffHomeViewModel,
    onNavigateToMarkAttendance: (Long) -> Unit,
    onNavigateToAttendanceHistory: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Portal") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()

                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                uiState.staff != null -> {
                    val staff = uiState.staff!!
                    val isEnrolled = staff.faceEmbedding != null

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Welcome,",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = staff.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = staff.employeeId,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Enrollment status badge
                        Surface(
                            color = if (isEnrolled)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = if (isEnrolled) "Face Enrolled ✓" else "Face Not Enrolled",
                                color = if (isEnrolled)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        Button(
                            onClick = { onNavigateToMarkAttendance(staff.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = isEnrolled
                        ) {
                            Text("Mark Attendance")
                        }

                        if (!isEnrolled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Contact your admin to enrol your face before marking attendance.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { onNavigateToAttendanceHistory(staff.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("View My Attendance")
                        }
                    }
                }
            }
        }
    }
}
