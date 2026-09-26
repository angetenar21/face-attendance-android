package com.salarybox.app.ui.staff

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.salarybox.app.data.local.entity.AttendanceEntity
import com.salarybox.app.data.repository.AttendanceRepository
import com.salarybox.app.data.repository.StaffRepository
import com.salarybox.app.util.FaceEmbeddingExtractor
import com.salarybox.app.util.FaceMatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import kotlin.math.max

data class MarkAttendanceUiState(
    val isProcessing: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val matchConfidence: Float = 0f,
    val address: String? = null
)

class MarkAttendanceViewModel(
    private val staffId: Long,
    private val staffRepository: StaffRepository,
    private val attendanceRepository: AttendanceRepository,
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarkAttendanceUiState())
    val uiState: StateFlow<MarkAttendanceUiState> = _uiState.asStateFlow()

    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .build()
    )
    
    private val embeddingExtractor = FaceEmbeddingExtractor(appContext)
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(appContext)

    @SuppressLint("MissingPermission")
    fun processAttendanceCapture(imagePath: String, hasLocationPermission: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null, success = false) }

            try {
                // 1. Fetch Staff & Enrolled Embedding
                val staff = staffRepository.getStaffById(staffId)
                if (staff?.faceEmbedding == null) {
                    _uiState.update { it.copy(isProcessing = false, error = "Staff member has no enrolled face.") }
                    return@launch
                }
                val enrolledEmbedding = FaceMatcher.parseEmbedding(staff.faceEmbedding)
                
                // 1.5 Throttle check: Block if checked in within the last 60 seconds
                val recentRecords = kotlinx.coroutines.flow.first(attendanceRepository.getAttendanceForStaff(staffId))
                val lastRecordTime = recentRecords.maxByOrNull { it.timestamp }?.timestamp ?: 0L
                if (System.currentTimeMillis() - lastRecordTime < 60_000) {
                    _uiState.update { it.copy(isProcessing = false, error = "You just marked attendance! Please wait a minute before trying again.") }
                    return@launch
                }

                // 2. Process captured image
                val file = File(imagePath)
                if (!file.exists()) {
                    _uiState.update { it.copy(isProcessing = false, error = "Captured photo not found.") }
                    return@launch
                }

                val bitmap = loadAndRotateBitmap(imagePath)
                if (bitmap == null) {
                    _uiState.update { it.copy(isProcessing = false, error = "Failed to load image file.") }
                    return@launch
                }

                // 3. Detect Face
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                val faces = faceDetector.process(inputImage).await()

                if (faces.isEmpty()) {
                    _uiState.update { it.copy(isProcessing = false, error = "No face detected in the photo.") }
                    return@launch
                }
                if (faces.size > 1) {
                    _uiState.update { it.copy(isProcessing = false, error = "Multiple faces detected.") }
                    return@launch
                }

                val face = faces.first()
                val boundingBox = face.boundingBox

                // 4. Crop Face & Extract Embedding
                val left = max(0, boundingBox.left)
                val top = max(0, boundingBox.top)
                val width = minOf(bitmap.width - left, boundingBox.width())
                val height = minOf(bitmap.height - top, boundingBox.height())
                val croppedFace = Bitmap.createBitmap(bitmap, left, top, width, height)
                
                val currentEmbedding = embeddingExtractor.extractEmbedding(croppedFace)

                // 5. Match Faces
                val similarity = FaceMatcher.cosineSimilarity(enrolledEmbedding, currentEmbedding)
                if (similarity < FaceMatcher.MATCH_THRESHOLD) {
                    _uiState.update { 
                        it.copy(
                            isProcessing = false, 
                            error = "Face not recognized. Please try again. (Confidence: ${"%.2f".format(similarity)})"
                        ) 
                    }
                    // Delete the photo since we won't save attendance
                    file.delete()
                    return@launch
                }

                // 6. Get Location
                var lat = 0.0
                var lon = 0.0
                if (hasLocationPermission) {
                    try {
                        val location = fusedLocationClient.lastLocation.await()
                        if (location != null) {
                            lat = location.latitude
                            this@MarkAttendanceViewModel.let {
                                lon = location.longitude
                            }
                        }
                    } catch (e: Exception) {
                        // Location failed, but we still mark attendance
                    }
                }

                // 7. Save Attendance
                // Move file to attendance folder
                val attendanceDir = File(appContext.filesDir, "attendance")
                attendanceDir.mkdirs()
                val finalFile = File(attendanceDir, "att_${System.currentTimeMillis()}.jpg")
                file.copyTo(finalFile, overwrite = true)
                file.delete()

                val attendance = AttendanceEntity(
                    staffId = staffId,
                    timestamp = System.currentTimeMillis(),
                    selfiePath = finalFile.absolutePath,
                    latitude = lat,
                    longitude = lon,
                    matchConfidence = similarity
                )
                attendanceRepository.insertAttendance(attendance)

                _uiState.update { 
                    it.copy(
                        isProcessing = false, 
                        success = true, 
                        matchConfidence = similarity
                    ) 
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isProcessing = false, error = "Error: ${e.message}") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        embeddingExtractor.close()
        faceDetector.close()
    }

    private fun loadAndRotateBitmap(imagePath: String): Bitmap? {
        val bitmap = BitmapFactory.decodeFile(imagePath) ?: return null
        val exif = ExifInterface(imagePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        
        return if (matrix.isIdentity) bitmap 
               else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}

class MarkAttendanceViewModelFactory(
    private val staffId: Long,
    private val staffRepository: StaffRepository,
    private val attendanceRepository: AttendanceRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MarkAttendanceViewModel(staffId, staffRepository, attendanceRepository, context) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkAttendanceScreen(
    viewModel: MarkAttendanceViewModel,
    onNavigateBack: () -> Unit,
    onLaunchCamera: () -> Unit,
    capturedImagePath: String?
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
                                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            
            // Launch camera right after permission is resolved
            onLaunchCamera()
        }
    )

    // Trigger processing when image comes back
    LaunchedEffect(capturedImagePath) {
        if (!capturedImagePath.isNullOrEmpty()) {
            viewModel.processAttendanceCapture(capturedImagePath, hasLocationPermission)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mark Attendance") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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
            when {
                uiState.isProcessing -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Verifying Identity...")
                }
                uiState.success -> {
                    Icon(
                        Icons.Filled.CheckCircle, 
                        contentDescription = "Success", 
                        tint = Color(0xFF4CAF50), 
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Attendance Marked Successfully!",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Confidence: ${"%.1f".format(uiState.matchConfidence * 100)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Done")
                    }
                }
                uiState.error != null -> {
                    Icon(
                        Icons.Filled.Error, 
                        contentDescription = "Error", 
                        tint = MaterialTheme.colorScheme.error, 
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.error!!,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = onLaunchCamera) {
                        Text("Try Again")
                    }
                }
                else -> {
                    // Initial state: ready to launch camera
                    Text(
                        text = "Take a selfie to mark your attendance. Your face will be matched against your enrolled profile.",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            if (!hasLocationPermission) {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            } else {
                                onLaunchCamera()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Start Camera")
                    }
                }
            }
        }
    }
}
