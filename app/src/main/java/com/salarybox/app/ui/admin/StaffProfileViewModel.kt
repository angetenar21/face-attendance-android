package com.salarybox.app.ui.admin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.salarybox.app.data.local.entity.StaffEntity
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

data class StaffProfileUiState(
    val isLoading: Boolean = true,
    val staff: StaffEntity? = null,
    val error: String? = null,
    val isEnrolling: Boolean = false,
    val enrollmentSuccess: Boolean = false,
    val enrolledSelfiePath: String? = null
)

class StaffProfileViewModel(
    private val staffId: Long,
    private val staffRepository: StaffRepository,
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffProfileUiState())
    val uiState: StateFlow<StaffProfileUiState> = _uiState.asStateFlow()
    
    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .build()
    )
    
    private val embeddingExtractor = FaceEmbeddingExtractor(appContext)

    init {
        loadStaff()
    }

    private fun loadStaff() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val staff = staffRepository.getStaffById(staffId)
            val selfieFile = File(appContext.filesDir, "enrolled_${staffId}.jpg")
            val selfiePath = if (selfieFile.exists()) selfieFile.absolutePath else null
            
            if (staff != null) {
                _uiState.update { it.copy(isLoading = false, staff = staff, enrolledSelfiePath = selfiePath) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Staff member not found") }
            }
        }
    }

    fun processEnrollmentPhoto(imagePath: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isEnrolling = true, error = null, enrollmentSuccess = false) }
            
            val file = File(imagePath)
            if (!file.exists()) {
                _uiState.update { it.copy(isEnrolling = false, error = "Captured photo not found.") }
                return@launch
            }

            try {
                // 1. Load bitmap and fix EXIF rotation
                val bitmap = loadAndRotateBitmap(imagePath)
                if (bitmap == null) {
                    _uiState.update { it.copy(isEnrolling = false, error = "Failed to load image file.") }
                    return@launch
                }

                // 2. Detect face accurately
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                val faces = faceDetector.process(inputImage).await()

                if (faces.isEmpty()) {
                    _uiState.update { it.copy(isEnrolling = false, error = "No face detected in the photo. Please try again.") }
                    return@launch
                }
                if (faces.size > 1) {
                    _uiState.update { it.copy(isEnrolling = false, error = "Multiple faces detected. Please ensure only one face is in the frame.") }
                    return@launch
                }

                val face = faces.first()
                val boundingBox = face.boundingBox

                // 3. Crop face
                val left = max(0, boundingBox.left)
                val top = max(0, boundingBox.top)
                val width = minOf(bitmap.width - left, boundingBox.width())
                val height = minOf(bitmap.height - top, boundingBox.height())
                
                if (width <= 0 || height <= 0) {
                     _uiState.update { it.copy(isEnrolling = false, error = "Invalid face bounds detected.") }
                     return@launch
                }

                val croppedFace = Bitmap.createBitmap(bitmap, left, top, width, height)

                // 4. Extract embedding
                val embedding = embeddingExtractor.extractEmbedding(croppedFace)
                val encodedEmbedding = FaceMatcher.encodeEmbedding(embedding)

                // 5. Save to database
                staffRepository.updateFaceEmbedding(
                    id = staffId,
                    embedding = encodedEmbedding,
                    enrolledAt = System.currentTimeMillis()
                )
                
                // 6. Save selfie for profile view
                val newFile = File(appContext.filesDir, "enrolled_${staffId}.jpg")
                file.copyTo(newFile, overwrite = true)
                file.delete()

                // 7. Reload staff to update UI
                val updatedStaff = staffRepository.getStaffById(staffId)
                _uiState.update { 
                    it.copy(
                        isEnrolling = false, 
                        enrollmentSuccess = true,
                        staff = updatedStaff,
                        enrolledSelfiePath = newFile.absolutePath
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isEnrolling = false, error = "Enrollment failed: ${e.message}") }
            }
        }
    }
    
    fun dismissSuccess() {
        _uiState.update { it.copy(enrollmentSuccess = false) }
    }
    
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
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

class StaffProfileViewModelFactory(
    private val staffId: Long,
    private val staffRepository: StaffRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StaffProfileViewModel::class.java)) {
            return StaffProfileViewModel(staffId, staffRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
