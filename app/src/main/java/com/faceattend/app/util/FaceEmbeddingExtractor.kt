package com.faceattend.app.util

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * Extracts facial embeddings using a LiteRT MobileFaceNet model.
 * 
 * MobileFaceNet provides a balance of speed and accuracy, generating a
 * 192-dimensional vector representation of a face. We use LiteRT (com.google.ai.edge.litert)
 * for the inference execution as requested.
 */
class FaceEmbeddingExtractor(context: Context) {
    private val interpreter: Interpreter
    
    init {
        // Load the tflite model from the assets folder.
        // Requires android.aaptOptions.noCompress "tflite" in build.gradle.kts to avoid compression.
        val assetFileDescriptor = context.assets.openFd("mobile_face_net.tflite")
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        val mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        
        interpreter = Interpreter(mappedByteBuffer)
    }

    /**
     * Extracts a 192-dim face embedding from a cropped face bitmap.
     * The input bitmap should ideally be cropped exactly to the bounding box
     * returned by ML Kit, and will be resized to 112x112 internally.
     */
    fun extractEmbedding(faceBitmap: Bitmap): FloatArray {
        // MobileFaceNet input is 112x112 RGB
        val resizedBitmap = Bitmap.createScaledBitmap(faceBitmap, 112, 112, true)
        
        // 1 batch, 112 height, 112 width, 3 channels, 4 bytes per float
        val inputBuffer = ByteBuffer.allocateDirect(1 * 112 * 112 * 3 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())
        
        val intValues = IntArray(112 * 112)
        resizedBitmap.getPixels(intValues, 0, 112, 0, 0, 112, 112)
        
        // MobileFaceNet normalization: (pixel - 127.5) / 128.0
        for (pixelValue in intValues) {
            val r = ((pixelValue shr 16 and 0xFF) - 127.5f) / 128.0f
            val g = ((pixelValue shr 8 and 0xFF) - 127.5f) / 128.0f
            val b = ((pixelValue and 0xFF) - 127.5f) / 128.0f
            
            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }
        
        // Output is typically [1, 192]
        val outputBuffer = Array(1) { FloatArray(192) }
        
        interpreter.run(inputBuffer, outputBuffer)
        
        return outputBuffer[0]
    }
    
    fun close() {
        interpreter.close()
    }
}
