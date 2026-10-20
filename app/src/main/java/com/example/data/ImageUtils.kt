package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {
    private const val TAG = "ImageUtils"
    private const val BASE64_PREFIX_JPEG = "data:image/jpeg;base64,"
    private const val BASE64_PREFIX_PNG = "data:image/png;base64,"

    /**
     * Converts a content:// or file:// Uri into a compressed JPEG Base64 string.
     * Also saves a local persistent backup file in app's internal storage so it is never lost.
     */
    fun uriToBase64(context: Context, uri: Uri, maxDimension: Int = 1200, quality: Int = 82): String? {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
            val rawBytes = inputStream.readBytes()
            inputStream.close()

            // Decode bounds to calculate sample size
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, options)

            var sampleSize = 1
            val maxSide = maxOf(options.outWidth, options.outHeight)
            while ((maxSide / sampleSize) > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }

            val bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOptions) ?: return null

            // Compress to JPEG
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val compressedBytes = outputStream.toByteArray()
            outputStream.close()

            // Also save persistent local cache file
            try {
                val receiptsDir = File(context.filesDir, "receipts").apply { mkdirs() }
                val persistentFile = File(receiptsDir, "receipt_${System.currentTimeMillis()}.jpg")
                FileOutputStream(persistentFile).use { fos ->
                    fos.write(compressedBytes)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to save local persistent receipt file: ${e.message}")
            }

            val base64Encoded = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
            BASE64_PREFIX_JPEG + base64Encoded
        } catch (e: Exception) {
            Log.e(TAG, "Error converting URI to Base64: ${e.message}", e)
            null
        }
    }

    /**
     * Decodes a Base64 string (with or without data:image prefix) into a Bitmap.
     */
    fun base64ToBitmap(base64String: String): Bitmap? {
        if (base64String.isBlank()) return null
        return try {
            val cleanBase64 = when {
                base64String.startsWith(BASE64_PREFIX_JPEG) -> base64String.removePrefix(BASE64_PREFIX_JPEG)
                base64String.startsWith(BASE64_PREFIX_PNG) -> base64String.removePrefix(BASE64_PREFIX_PNG)
                base64String.startsWith("data:image/") -> {
                    val commaIndex = base64String.indexOf(",")
                    if (commaIndex != -1) base64String.substring(commaIndex + 1) else base64String
                }
                else -> base64String
            }.trim()

            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding Base64 to Bitmap: ${e.message}")
            null
        }
    }

    /**
     * Checks if a string is a Base64 image payload.
     */
    fun isBase64Image(str: String): Boolean {
        if (str.isBlank()) return false
        if (str.startsWith("data:image/")) return true
        if (str.length > 300 && !str.startsWith("http://") && !str.startsWith("https://") && !str.startsWith("content://") && !str.startsWith("file://")) {
            return true
        }
        return false
    }

    /**
     * Loads a Bitmap from either a Base64 string, file path, or content URI.
     */
    fun loadReceiptBitmap(context: Context, imageSource: String): Bitmap? {
        if (imageSource.isBlank()) return null
        
        // 1. Try Base64
        if (isBase64Image(imageSource)) {
            val bmp = base64ToBitmap(imageSource)
            if (bmp != null) return bmp
        }

        // 2. Try Local File
        if (imageSource.startsWith("file://") || imageSource.startsWith("/")) {
            try {
                val path = imageSource.removePrefix("file://")
                val file = File(path)
                if (file.exists()) {
                    val bmp = BitmapFactory.decodeFile(file.absolutePath)
                    if (bmp != null) return bmp
                }
            } catch (_: Exception) {}
        }

        // 3. Try Content URI
        if (imageSource.startsWith("content://")) {
            try {
                val uri = Uri.parse(imageSource)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    if (bmp != null) return bmp
                }
            } catch (_: Exception) {}
        }

        return null
    }
}
