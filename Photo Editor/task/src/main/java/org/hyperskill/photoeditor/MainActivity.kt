package org.hyperskill.photoeditor

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import org.hyperskill.photoeditor.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val activityResultLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val photoUri = result.data?.data ?: return@registerForActivityResult
            binding.ivPhoto.setImageURI(photoUri)
            originalBitmap = binding.ivPhoto.drawToBitmap()
            binding.slBrightness.value = 0f
        }
    }

    private lateinit var originalBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        originalBitmap = createBitmap()
        binding.ivPhoto.setImageBitmap(originalBitmap)

        setupActionListeners()
        setupOnChangeListeners()
    }

    private fun createBitmap(): Bitmap {
        val width = 200
        val height = 100
        val pixels = IntArray(width * height)
        // get pixel array from source

        var R: Int
        var G: Int
        var B: Int
        var index: Int

        for (y in 0 until height) {
            for (x in 0 until width) {
                // get current index in 2D-matrix
                index = y * width + x
                // get color
                R = x % 100 + 40
                G = y % 100 + 80
                B = (x + y) % 100 + 120

                pixels[index] = Color.rgb(R, G, B)

            }
        }
        // output bitmap
        val bitmapOut = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmapOut.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmapOut
    }

    private fun setupActionListeners() = with(binding) {
        btnGallery.setOnClickListener {
            activityResultLauncher.launch(
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
            )
        }
    }

    private fun setupOnChangeListeners() = with(binding) {
        slBrightness.addOnChangeListener { _, value, _ ->

            val (width, height) = with(originalBitmap) { width to height }
            val pixels = IntArray(width * height)
            originalBitmap.getPixels(
                pixels, 0, width, 0, 0, width, height
            )

            val intValue = value.toInt()

            for (i in pixels.indices) {
                val pixel = pixels[i]
                val alpha = Color.alpha(pixel)
                var red = Color.red(pixel) + intValue
                var green = Color.green(pixel) + intValue
                var blue = Color.blue(pixel) + intValue

                // Apply brightness correction with limits
                red = red.coerceIn(0, 255)
                green = green.coerceIn(0, 255)
                blue = blue.coerceIn(0, 255)

                pixels[i] = Color.argb(alpha, red, green, blue)
            }

            val resultBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            ivPhoto.setImageBitmap(resultBitmap)
        }
    }
}
