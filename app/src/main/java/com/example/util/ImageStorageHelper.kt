package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

object ImageStorageHelper {

    private const val TAG = "ImageStorageHelper"

    /**
     * Loads and safely downsamples a bitmap from a URI to avoid OutOfMemoryError.
     */
    suspend fun decodeBitmapFromUri(
        context: Context,
        uri: Uri,
        maxDimension: Int = 2048
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // First decode bounds
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            val srcWidth = options.outWidth
            val srcHeight = options.outHeight
            if (srcWidth <= 0 || srcHeight <= 0) return@withContext null

            // Calculate sample size
            var sampleSize = 1
            while (srcWidth / sampleSize > maxDimension || srcHeight / sampleSize > maxDimension) {
                sampleSize *= 2
            }

            // Decode actual bitmap
            inputStream = context.contentResolver.openInputStream(uri)
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val originalBitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (originalBitmap == null) return@withContext null

            // Check EXIF orientation
            var orientedBitmap = originalBitmap
            try {
                val exifStream = context.contentResolver.openInputStream(uri)
                if (exifStream != null) {
                    val exif = ExifInterface(exifStream)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    exifStream.close()

                    val matrix = Matrix()
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                    }
                    if (!matrix.isIdentity) {
                        orientedBitmap = Bitmap.createBitmap(
                            originalBitmap,
                            0,
                            0,
                            originalBitmap.width,
                            originalBitmap.height,
                            matrix,
                            true
                        )
                        if (orientedBitmap != originalBitmap) {
                            originalBitmap.recycle()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "EXIF rotation check failed", e)
            }

            orientedBitmap
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode bitmap from URI: $uri", e)
            null
        }
    }

    /**
     * Saves a cropped bitmap into the app's permanent internal storage directory.
     * Returns a permanent file URI that will never expire across app restarts.
     */
    suspend fun saveCroppedBitmap(
        context: Context,
        bitmap: Bitmap,
        prefix: String = "app_bg"
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val bgDir = File(context.filesDir, "backgrounds").apply {
                if (!exists()) mkdirs()
            }

            // Cleanup older files with same prefix to keep storage tidy
            bgDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("custom_${prefix}_")) {
                    file.delete()
                }
            }

            val targetFile = File(bgDir, "custom_${prefix}_${System.currentTimeMillis()}.png")
            FileOutputStream(targetFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            }

            Uri.fromFile(targetFile)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save cropped bitmap", e)
            null
        }
    }

    /**
     * Copies user-selected audio from content URI to permanent internal storage.
     * Returns Pair(displayName, internalFileUriString).
     */
    suspend fun copyAudioToInternalStorage(
        context: Context,
        sourceUri: Uri
    ): Pair<String, String>? = withContext(Dispatchers.IO) {
        try {
            var displayName = "Custom Audio.mp3"
            try {
                context.contentResolver.query(sourceUri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) {
                        val name = cursor.getString(nameIndex)
                        if (!name.isNullOrBlank()) {
                            displayName = name
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not query audio display name", e)
            }

            val cleanName = displayName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val tonesDir = File(context.filesDir, "custom_tones").apply {
                if (!exists()) mkdirs()
            }
            val targetFile = File(tonesDir, "tone_${System.currentTimeMillis()}_$cleanName")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            }

            val savedUri = Uri.fromFile(targetFile).toString()
            Pair(displayName, savedUri)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy audio to internal storage", e)
            null
        }
    }
}
