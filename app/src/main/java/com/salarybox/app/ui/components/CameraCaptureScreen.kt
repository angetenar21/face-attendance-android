package com.salarybox.app.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.util.Size as AndroidSize
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Reusable CameraX capture screen with ML Kit Face Detection.
 */
@Composable
fun CameraCaptureScreen(
    onImageCaptured: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var permissionRequested by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasPermission = granted
            permissionRequested = true
        }
    )

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasPermission) {
        CameraPreviewContent(onImageCaptured = onImageCaptured, onCancel = onCancel)
    } else {
        val activity = context as? android.app.Activity
        val isPermanentlyDenied = permissionRequested && activity?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) == false

        PermissionDeniedContent(
            isPermanentlyDenied = isPermanentlyDenied,
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            onOpenSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            },
            onCancel = onCancel
        )
    }
}

@Composable
private fun PermissionDeniedContent(
    isPermanentlyDenied: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "SalaryBox needs camera access to capture face selfies for attendance and enrollment.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (isPermanentlyDenied) {
            Button(onClick = onOpenSettings) {
                Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Settings")
            }
        } else {
            Button(onClick = onRequestPermission) {
                Text("Grant Permission")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onCancel) {
            Text("Cancel")
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraPreviewContent(
    onImageCaptured: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    // Face detection state
    var detectedFaces by remember { mutableStateOf<List<Face>>(emptyList()) }
    var imageProxySize by remember { mutableStateOf(AndroidSize(1, 1)) }
    var previewSize by remember { mutableStateOf(IntSize(1, 1)) }
    
    val faceDetector = remember {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onGloballyPositioned { coordinates ->
                previewSize = coordinates.size
            }
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    imageCapture = capture

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        
                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val rotation = imageProxy.imageInfo.rotationDegrees
                            // Keep track of the image dimension as seen by the analyzer.
                            // When rotated 90 or 270 degrees, width and height are swapped relative to the screen.
                            if (rotation == 90 || rotation == 270) {
                                imageProxySize = AndroidSize(imageProxy.height, imageProxy.width)
                            } else {
                                imageProxySize = AndroidSize(imageProxy.width, imageProxy.height)
                            }
                            
                            val inputImage = InputImage.fromMediaImage(mediaImage, rotation)
                            faceDetector.process(inputImage)
                                .addOnSuccessListener { faces ->
                                    detectedFaces = faces
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    // Selfie mode as default
                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            capture,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("CameraCapture", "Use case binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Draw bounding boxes
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleX = size.width / imageProxySize.width.toFloat()
            val scaleY = size.height / imageProxySize.height.toFloat()
            
            // FILL_CENTER crops the image to fill the screen, so the actual scale used is max(scaleX, scaleY).
            val scale = maxOf(scaleX, scaleY)
            
            // Calculate offsets to center the scaled image bounds within the canvas
            val scaledImageWidth = imageProxySize.width * scale
            val scaledImageHeight = imageProxySize.height * scale
            val offsetX = (size.width - scaledImageWidth) / 2f
            val offsetY = (size.height - scaledImageHeight) / 2f

            for (face in detectedFaces) {
                val box = face.boundingBox
                
                // Front camera is mirrored horizontally. ML Kit coordinates are relative to the original image.
                // After rotation, X=0 is the left of the image. For front camera, this appears on the right side of the screen.
                // We need to mirror the X coordinate.
                
                val left = (box.left * scale) + offsetX
                val right = (box.right * scale) + offsetX
                
                val mirroredLeft = size.width - right
                val mirroredRight = size.width - left
                
                val top = (box.top * scale) + offsetY
                val bottom = (box.bottom * scale) + offsetY
                
                drawRect(
                    color = Color.Green,
                    topLeft = Offset(mirroredLeft, top),
                    size = Size(mirroredRight - mirroredLeft, bottom - top),
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        }
        
        // Status Text
        val isFaceReady = detectedFaces.size == 1
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            val text = when (detectedFaces.size) {
                0 -> "No face detected"
                1 -> "Face detected - Ready"
                else -> "Multiple faces detected"
            }
            val bgColor = if (isFaceReady) Color(0xFF4CAF50).copy(alpha = 0.8f) else Color.Red.copy(alpha = 0.8f)
            
            Text(
                text = text,
                color = Color.White,
                modifier = Modifier
                    .background(bgColor, CircleShape)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // UI Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text("Cancel")
                }

                // Capture Button
                IconButton(
                    onClick = {
                        if (isCapturing || !isFaceReady) return@IconButton
                        val capture = imageCapture ?: return@IconButton
                        isCapturing = true

                        val selfiesDir = File(context.filesDir, "selfies").apply { mkdirs() }
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
                        val photoFile = File(selfiesDir, "selfie_$timestamp.jpg")

                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                        capture.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    onImageCaptured(photoFile.absolutePath)
                                    isCapturing = false
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    Log.e("CameraCapture", "Photo capture failed", exception)
                                    isCapturing = false
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.White.copy(alpha = 0.3f), CircleShape)
                        .padding(8.dp)
                        .background(
                            color = if (isCapturing || !isFaceReady) Color.LightGray else Color.White,
                            shape = CircleShape
                        ),
                    enabled = isFaceReady && !isCapturing
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = "Take photo",
                            tint = if (isFaceReady) Color.Black else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(64.dp))
            }
        }
    }
}
