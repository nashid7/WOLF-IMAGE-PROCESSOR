package com.example.new1

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import java.io.File
import java.io.FileOutputStream

class ImageProcessor {
    fun addSerialNumberToImage(bitmap: Bitmap, serialNumber: String): Bitmap {
        // Create a new bitmap with extra space at the top for the serial number
        val result = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height + 200, // Add 200 pixels at the top
            Bitmap.Config.ARGB_8888
        )
        
        val canvas = Canvas(result)
        
        // Draw white background for the serial number area
        val whiteBackground = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, bitmap.width.toFloat(), 200f, whiteBackground)
        
        // Draw the original image below the white area
        canvas.drawBitmap(bitmap, 0f, 200f, null)
        
        // Draw the serial number
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 100f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        
        // Calculate text position
        val textBounds = Rect()
        textPaint.getTextBounds(serialNumber, 0, serialNumber.length, textBounds)
        val x = (bitmap.width - textBounds.width()) / 2f
        val y = 150f // Position from top
        
        canvas.drawText(serialNumber, x, y, textPaint)
        return result
    }
    
    fun saveImage(bitmap: Bitmap, outputFile: File): Boolean {
        return try {
            FileOutputStream(outputFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
} 