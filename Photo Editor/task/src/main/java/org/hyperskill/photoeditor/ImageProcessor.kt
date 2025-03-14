package org.hyperskill.photoeditor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.IOException

class ImageProcessor(private val context: Context, private var loadedImage: Bitmap) {

    private val _processedImage = MutableStateFlow<Bitmap>(loadedImage)
    val processedImage = _processedImage.asStateFlow()

    fun updateImage(uri: Uri) {
        val bitmap = BitmapFactory
            .decodeStream(context.contentResolver.openInputStream(uri))
            ?: throw IOException("No such file or Incorrect format")

        loadedImage = bitmap
        _processedImage.update { bitmap }
    }

    fun changeBrightness(value: Float = 0f) {
        with(loadedImage) {
            if (value == 0f) {
                return@with
            }

            val pixelBuffer = IntArray(width * height)
            getPixels(pixelBuffer, 0, width, 0, 0, width, height)

            val newImage = copy(Bitmap.Config.ARGB_8888, true)
            val intValue = value.toInt()

            for (i in pixelBuffer.indices) {
                val pixel = pixelBuffer[i]
                val red = pixel.red.plusAndCoerce(intValue)
                val green = pixel.green.plusAndCoerce(intValue)
                val blue = pixel.blue.plusAndCoerce(intValue)
                pixelBuffer[i] = Color.rgb(red, green, blue)
            }

            newImage.setPixels(pixelBuffer, 0, width, 0, 0, width, height)
            _processedImage.update { newImage }
        }
    }

    fun changeContrast(value: Float) {
        with(_processedImage.value) {
            if (value == 0f) {
                return@with
            }

            val pixelBuffer = IntArray(width * height)
            getPixels(pixelBuffer, 0, width, 0, 0, width, height)

            val newImage = copy(Bitmap.Config.ARGB_8888, true)
            val alpha = (255 + value) / (255 - value)

            val avgBright = pixelBuffer
                .sumOf { pixel ->
                    Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)
                }
                .div(pixelBuffer.size * 3)

            for (i in pixelBuffer.indices) {
                val pixel = pixelBuffer[i]

                val red = (alpha * (pixel.red - avgBright) + avgBright)
                    .toInt()
                    .plusAndCoerce()

                val green = (alpha * (pixel.green - avgBright) + avgBright)
                    .toInt()
                    .plusAndCoerce()

                val blue = (alpha * (pixel.blue - avgBright) + avgBright)
                    .toInt()
                    .plusAndCoerce()

                pixelBuffer[i] = Color.rgb(red, green, blue)
            }

            newImage.setPixels(pixelBuffer, 0, width, 0, 0, width, height)
            _processedImage.update { newImage }
        }
    }

    private fun Int.plusAndCoerce(value: Int = 0): Int {
        val result = this + value
        return result.coerceIn(0, 255)
    }
}
