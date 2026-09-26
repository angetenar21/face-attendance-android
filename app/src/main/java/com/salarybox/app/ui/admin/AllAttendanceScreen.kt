package com.salarybox.app.ui.admin

import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.salarybox.app.data.repository.AttendanceRepository
import com.salarybox.app.data.repository.StaffRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class AllAttendanceRecordUi(
    val id: Long,
    val staffName: String,
    val timestamp: Long,
    val selfiePath: String,
    val address: String,
    val confidence: Float
)

data class AllAttendanceUiState(
    val records: List<AllAttendanceRecordUi> = emptyList(),
    val isLoading: Boolean = true
)

class AllAttendanceViewModel(
    private val staffRepository: StaffRepository,
    private val attendanceRepository: AttendanceRepository,
    private val appContext: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AllAttendanceUiState())
    val uiState: StateFlow<AllAttendanceUiState> = _uiState.asStateFlow()

    private val addressCache = ConcurrentHashMap<Pair<Double, Double>, String>()

    init {
        loadAllAttendance()
    }

    private fun loadAllAttendance() {
        viewModelScope.launch {
            combine(
                attendanceRepository.getAllAttendance(),
                staffRepository.getAllStaff()
            ) { attendances, staffs ->
                val staffMap = staffs.associateBy { it.id }
                
                val uiRecords = attendances.sortedByDescending { it.timestamp }.map { entity ->
                    val staffName = staffMap[entity.staffId]?.name ?: "Unknown Staff"
                    val address = resolveAddressCached(entity.latitude, entity.longitude)
                    
                    AllAttendanceRecordUi(
                        id = entity.id,
                        staffName = staffName,
                        timestamp = entity.timestamp,
                        selfiePath = entity.selfiePath,
                        address = address,
                        confidence = entity.matchConfidence
                    )
                }
                uiRecords
            }.collect { records ->
                _uiState.update { it.copy(records = records, isLoading = false) }
            }
        }
    }

    private suspend fun resolveAddressCached(lat: Double, lon: Double): String {
        if (lat == 0.0 && lon == 0.0) return "Location Unavailable"
        val key = Pair(lat, lon)
        addressCache[key]?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(appContext, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                val addressStr = if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    address.getAddressLine(0) ?: "${lat}, ${lon}"
                } else {
                    "${lat}, ${lon}"
                }
                addressCache[key] = addressStr
                addressStr
            } catch (e: Exception) {
                "${lat}, ${lon}"
            }
        }
    }
}

class AllAttendanceViewModelFactory(
    private val staffRepository: StaffRepository,
    private val attendanceRepository: AttendanceRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AllAttendanceViewModel(staffRepository, attendanceRepository, context) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllAttendanceScreen(
    viewModel: AllAttendanceViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Attendance") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.records.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No attendance records found.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.records, key = { it.id }) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = record.selfiePath,
                                contentDescription = "Attendance Selfie",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = record.staffName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = dateFormat.format(Date(record.timestamp)),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp).padding(top = 2.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = record.address,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "Match Confidence: ${"%.1f".format(record.confidence * 100)}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
