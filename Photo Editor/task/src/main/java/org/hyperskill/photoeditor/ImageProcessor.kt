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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import java.io.IOException
import kotlin.math.pow

class ImageProcessor(private val context: Context, loadedImage: Bitmap) {

    private val _processedImage = MutableStateFlow<Bitmap>(loadedImage)
    private val _brightnessFilterValue = MutableStateFlow(0f)
    private val _contrastFilterValue = MutableStateFlow(0f)
    private val _saturationFilterValue = MutableStateFlow(0f)
    private val _gammaFilterValue = MutableStateFlow(1f)

    val processedImage = combine(
        _processedImage,
        _brightnessFilterValue,
        _contrastFilterValue,
        _saturationFilterValue,
        _gammaFilterValue
    ) { image, brightness, contrast, saturation, gamma ->

        image
            .changeBrightnessInternally(brightness.toInt())
            .changeContrastInternally(contrast)
            .changeSaturationInternally(saturation)
            .changeGammaInternally(gamma.toDouble())
    }

    fun setImage(imageUri: Uri) {
        val bitmap = BitmapFactory
            .decodeStream(context.contentResolver.openInputStream(imageUri))
            ?: throw IOException("No such file or Incorrect format")

        _processedImage.update { bitmap }
    }

    fun changeBrightness(value: Float) {
        _brightnessFilterValue.update { value }
    }

    fun changeContrast(value: Float) {
        _contrastFilterValue.update { value }
    }

    fun changeSaturation(value: Float) {
        _saturationFilterValue.update { value }
    }

    fun changeGamma(value: Float) {
        _gammaFilterValue.update { value }
    }

    private fun Bitmap.changeBrightnessInternally(value: Int = 0): Bitmap {
        if (value == 0) {
            return this
        }

        val pixelBuffer = IntArray(width * height)
        getPixels(pixelBuffer, 0, width, 0, 0, width, height)

        val newImage = copy(Bitmap.Config.ARGB_8888, true)

        for (i in pixelBuffer.indices) {
            val pixel = pixelBuffer[i]
            val red = pixel.red.plusAndCoerce(value)
            val green = pixel.green.plusAndCoerce(value)
            val blue = pixel.blue.plusAndCoerce(value)
            pixelBuffer[i] = Color.rgb(red, green, blue)
        }

        newImage.setPixels(pixelBuffer, 0, width, 0, 0, width, height)
        return newImage
    }

    private fun Bitmap.changeContrastInternally(value: Float = 0f): Bitmap {
        if (value == 0f) {
            return this
        }

        val pixelBuffer = IntArray(width * height)
        getPixels(pixelBuffer, 0, width, 0, 0, width, height)

        val newImage = copy(Bitmap.Config.ARGB_8888, true)
        val contrastFactor = (255 + value) / (255 - value)

        val avgBright = pixelBuffer
            .sumOf { pixel -> pixel.red + pixel.green + pixel.blue }
            .div(pixelBuffer.size * 3)

        for (i in pixelBuffer.indices) {
            val pixel = pixelBuffer[i]

            val red = (contrastFactor * (pixel.red - avgBright) + avgBright)
                .toInt()
                .plusAndCoerce()

            val green = (contrastFactor * (pixel.green - avgBright) + avgBright)
                .toInt()
                .plusAndCoerce()

            val blue = (contrastFactor * (pixel.blue - avgBright) + avgBright)
                .toInt()
                .plusAndCoerce()

            pixelBuffer[i] = Color.rgb(red, green, blue)
        }

        newImage.setPixels(pixelBuffer, 0, width, 0, 0, width, height)
        return newImage
    }

    private fun Bitmap.changeSaturationInternally(value: Float = 0f): Bitmap {
        if (value == 0f) {
            return this
        }

        val pixelBuffer = IntArray(width * height)
        getPixels(pixelBuffer, 0, width, 0, 0, width, height)

        val newImage = copy(Bitmap.Config.ARGB_8888, true)
        val saturationFactor = (255 + value) / (255 - value)

        for (i in pixelBuffer.indices) {
            val pixel = pixelBuffer[i]

            val rgbAvg = listOf(pixel.red, pixel.green, pixel.blue).average()

            val red = (saturationFactor * (pixel.red - rgbAvg) + rgbAvg)
                .toInt()
                .plusAndCoerce()

            val green = (saturationFactor * (pixel.green - rgbAvg) + rgbAvg)
                .toInt()
                .plusAndCoerce()

            val blue = (saturationFactor * (pixel.blue - rgbAvg) + rgbAvg)
                .toInt()
                .plusAndCoerce()

            pixelBuffer[i] = Color.rgb(red, green, blue)
        }

        newImage.setPixels(pixelBuffer, 0, width, 0, 0, width, height)
        return newImage
    }

    private fun Bitmap.changeGammaInternally(value: Double = 1.0): Bitmap {
        if (value == 1.0) {
            return this
        }

        val pixelBuffer = IntArray(width * height)
        getPixels(pixelBuffer, 0, width, 0, 0, width, height)

        val newImage = copy(Bitmap.Config.ARGB_8888, true)

        for (i in pixelBuffer.indices) {
            val pixel = pixelBuffer[i]

            val red = (255 * (pixel.red.toDouble() / 255).pow(value))
                .toInt()
                .plusAndCoerce()

            val green = (255 * (pixel.green.toDouble() / 255).pow(value))
                .toInt()
                .plusAndCoerce()

            val blue = (255 * (pixel.blue.toDouble() / 255).pow(value))
                .toInt()
                .plusAndCoerce()

            pixelBuffer[i] = Color.rgb(red, green, blue)
        }

        newImage.setPixels(pixelBuffer, 0, width, 0, 0, width, height)
        return newImage
    }

    private fun Int.plusAndCoerce(value: Int = 0): Int {
        val result = this + value
        return result.coerceIn(0, 255)
    }
}
