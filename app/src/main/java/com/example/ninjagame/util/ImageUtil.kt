package com.example.ninjagame.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtil {
    private const val MAX_AVATAR_SIZE = 250 // Size tối ưu cho Avatar game

    suspend fun processImageToBase64(inputStream: InputStream?): String? = withContext(Dispatchers.IO) {
        try {
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return@withContext null
            
            // 1. Resize: Giữ tỉ lệ và ép về kích thước nhỏ
            val width = originalBitmap.width
            val height = originalBitmap.height
            val ratio = width.toFloat() / height.toFloat()
            
            val targetWidth: Int
            val targetHeight: Int
            if (width > height) {
                targetWidth = MAX_AVATAR_SIZE
                targetHeight = (MAX_AVATAR_SIZE / ratio).toInt()
            } else {
                targetHeight = MAX_AVATAR_SIZE
                targetWidth = (MAX_AVATAR_SIZE * ratio).toInt()
            }
            
            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
            
            // 2. Compress: Nén JPEG 70% (Cân bằng giữa chất lượng và dung lượng)
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            
            // 3. Encode: Dùng NO_WRAP để tránh ký tự xuống dòng trong DB
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun decodeBase64ToBitmap(base64: String?): Bitmap? {
        if (base64.isNullOrEmpty()) return null
        return try {
            val imageBytes = Base64.decode(base64, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: Exception) {
            null
        }
    }
}
