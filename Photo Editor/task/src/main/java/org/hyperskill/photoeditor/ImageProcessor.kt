package org.hyperskill.photoeditor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.system.measureTimeMillis

class ImageProcessor(
    private val context: AppCompatActivity,
    private var loadedImage: Bitmap
) {

    private val _processedImage = MutableStateFlow<Bitmap>(loadedImage)
    private val _filterSettings = MutableStateFlow(FilterSettings.default)

    private var filterJob: Job? = null

    val processedImage = _processedImage
        .onStart { setupFilterJob() }
        .stateIn(
            scope = context.lifecycleScope,
            started = SharingStarted.Lazily,
            initialValue = loadedImage
        )

    val filterSettings = _filterSettings.asStateFlow()

    fun setImage(imageUri: Uri) {
        val bitmap = decodeSampledBitmapFromUri(
            imageUri = imageUri,
            maxWidth = MAX_IMAGE_WIDTH_PX,
            maxHeight = MAX_IMAGE_HEIGHT_PX
        ) ?: return

        loadedImage = bitmap
        _processedImage.update { bitmap }
        _filterSettings.update { FilterSettings.default }
    }

    fun changeBrightness(value: Float) {
        _filterSettings.update { it.copy(brightness = value) }
    }

    fun changeContrast(value: Float) {
        _filterSettings.update { it.copy(contrast = value) }
    }

    fun changeSaturation(value: Float) {
        _filterSettings.update { it.copy(saturation = value) }
    }

    fun changeGamma(value: Float) {
        _filterSettings.update { it.copy(gamma = value) }
    }

    private fun setupFilterJob() {
        _filterSettings.onEach {
            doFilter(it)
        }.launchIn(context.lifecycleScope)
    }

    private fun doFilter(filterSettings: FilterSettings) {
        filterJob?.cancel()
        filterJob = context.lifecycleScope.launch(Dispatchers.Default) {
            val result = with(filterSettings) {
                loadedImage
                    .changeBrightnessInternally(brightness.toInt())
                    .changeContrastInternally(contrast)
                    .changeSaturationInternally(saturation)
                    .changeGammaInternally(gamma.toDouble())
            }

            ensureActive()
            _processedImage.update { result }
        }
    }

    private fun Bitmap.changeBrightnessInternally(value: Int): Bitmap {
        if (value == 0) {
            return this
        }

        val newImage = copy(Bitmap.Config.ARGB_8888, true)
        val duration = measureTimeMillis {
            val pixelBuffer = IntArray(width * height)
            getPixels(pixelBuffer, 0, width, 0, 0, width, height)


            for (i in pixelBuffer.indices) {
                val pixel = pixelBuffer[i]
                val red = pixel.red.plusAndCoerce(value)
                val green = pixel.green.plusAndCoerce(value)
                val blue = pixel.blue.plusAndCoerce(value)
                pixelBuffer[i] = Color.rgb(red, green, blue)
            }

            newImage.setPixels(pixelBuffer, 0, width, 0, 0, width, height)
        }

        Log.d(TAG, "Brightness filtering millis: $duration")
        return newImage
    }

    private fun Bitmap.changeContrastInternally(value: Float): Bitmap {
        if (value == 0f) {
            return this
        }

        val newImage = copy(Bitmap.Config.ARGB_8888, true)
        val duration = measureTimeMillis {
            val pixelBuffer = IntArray(width * height)
            getPixels(pixelBuffer, 0, width, 0, 0, width, height)

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
        }

        Log.d(TAG, "Contrast filtering millis: $duration")
        return newImage
    }

    private fun Bitmap.changeSaturationInternally(value: Float): Bitmap {
        if (value == 0f) {
            return this
        }

        val newImage = copy(Bitmap.Config.ARGB_8888, true)
        val duration = measureTimeMillis {
            val pixelBuffer = IntArray(width * height)
            getPixels(pixelBuffer, 0, width, 0, 0, width, height)

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
        }

        Log.d(TAG, "Saturation filtering millis: $duration")
        return newImage
    }

    private fun Bitmap.changeGammaInternally(value: Double): Bitmap {
        if (value == 1.0) {
            return this
        }

        val newImage = copy(Bitmap.Config.ARGB_8888, true)
        val duration = measureTimeMillis {
            val pixelBuffer = IntArray(width * height)
            getPixels(pixelBuffer, 0, width, 0, 0, width, height)


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
        }

        Log.d(TAG, "Gamma filtering millis: $duration")
        return newImage
    }

    private fun Int.plusAndCoerce(value: Int = 0): Int {
        val result = this + value
        return result.coerceIn(0, 255)
    }

    fun decodeSampledBitmapFromUri(
        imageUri: Uri,
        maxWidth: Int,
        maxHeight: Int
    ): Bitmap? {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(imageUri), null, this
            )

            Log.d(TAG, "Width: $outWidth, height: $outHeight")

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, maxWidth, maxHeight)
            Log.d(TAG, "InSampleSize: $inSampleSize")

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(imageUri), null, this
            ).also {
                Log.d(TAG, "width: $outWidth, height: $outHeight")
            }
        }
    }

    fun calculateInSampleSize(
        options: BitmapFactory.Options,
        maxWidth: Int,
        maxHeight: Int
    ): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        if (height > maxHeight || width > maxWidth) {
            var inSampleSize = 1
            while (
                (height / inSampleSize) >= maxHeight ||
                (width / inSampleSize) >= maxWidth
            ) {
                inSampleSize *= 2
            }

            return inSampleSize
        }

        return 1
    }

    companion object {
        private const val TAG = "ImageProcessor"
        private const val MAX_IMAGE_WIDTH_PX = 1000
        private const val MAX_IMAGE_HEIGHT_PX = 1000
    }
}
