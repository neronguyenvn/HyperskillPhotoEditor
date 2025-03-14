package org.hyperskill.photoeditor

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore.Images
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.drawToBitmap
import org.hyperskill.photoeditor.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val galleryLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { photoUri ->
                binding.ivPhoto.setImageURI(photoUri)
                currentBitmap = binding.ivPhoto.drawToBitmap()
                binding.slBrightness.value = 0f
            }
        }
    }

    private lateinit var currentBitmap: Bitmap
    private lateinit var brightnessAppliedBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        currentBitmap = createBitmap()
        binding.ivPhoto.setImageBitmap(currentBitmap)

        setupActionListeners()
        setupBrightnessChangeListener()
        setupContrastChangeListener()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            saveCurrentPhoto()
        }
    }

    private fun createBitmap(): Bitmap {
        val width = 200
        val height = 100
        val pixels = IntArray(width * height)

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
            launchGallery()
        }
        btnSave.setOnClickListener {
            if (hasWriteExternalPermission()) {
                saveCurrentPhoto()
            } else {
                requestWriteExternalPermission()
            }
        }
    }


    private fun launchGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            Images.Media.EXTERNAL_CONTENT_URI
        )
        galleryLauncher.launch(galleryIntent)
    }

    private fun setupBrightnessChangeListener() = with(binding) {
        slBrightness.addOnChangeListener { _, value, _ ->
            adjustBrightness(value.toInt())
            adjustContrast(slContrast.value.toInt())
        }
    }

    private fun setupContrastChangeListener() = with(binding) {
        slContrast.addOnChangeListener { _, value, _ ->
            adjustBrightness(slBrightness.value.toInt())
            adjustContrast(value.toInt())
        }
    }

    private fun adjustBrightness(brightnessValue: Int) {
        val width = currentBitmap.width
        val height = currentBitmap.height
        val pixels = IntArray(width * height)

        currentBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = Color.alpha(pixel)
            var red = Color.red(pixel) + brightnessValue
            var green = Color.green(pixel) + brightnessValue
            var blue = Color.blue(pixel) + brightnessValue

            // Apply brightness correction with limits
            red = red.coerceIn(0, 255)
            green = green.coerceIn(0, 255)
            blue = blue.coerceIn(0, 255)

            pixels[i] = Color.argb(alpha, red, green, blue)
        }

        brightnessAppliedBitmap = currentBitmap
            .copy(Bitmap.Config.ARGB_8888, true)
            .apply { setPixels(pixels, 0, width, 0, 0, width, height) }

        binding.ivPhoto.setImageBitmap(brightnessAppliedBitmap)
    }

    private fun adjustContrast(contrastValue: Int) {
        val width = currentBitmap.width
        val height = currentBitmap.height
        val pixels = IntArray(width * height)

        brightnessAppliedBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val alpha = (255 + contrastValue).toDouble() / (255 - contrastValue)

        val avgBright = pixels
            .sumOf { pixel ->
                Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)
            }
            .div(pixels.size * 3).toInt()


        for (i in pixels.indices) {
            val pixel = pixels[i]
            var red = (alpha * (Color.red(pixel) - avgBright) + avgBright).toInt()
            var green = (alpha * (Color.green(pixel) - avgBright) + avgBright).toInt()
            var blue = (alpha * (Color.blue(pixel) - avgBright) + avgBright).toInt()

            // Apply brightness correction with limits
            red = red.coerceIn(0, 255)
            green = green.coerceIn(0, 255)
            blue = blue.coerceIn(0, 255)

            pixels[i] = Color.argb(Color.alpha(pixel), red, green, blue)
        }


        brightnessAppliedBitmap.copy(Bitmap.Config.ARGB_8888, true).let { result ->
            result.setPixels(pixels, 0, width, 0, 0, width, height)
            binding.ivPhoto.setImageBitmap(result)
        }
    }

    private fun hasWriteExternalPermission(): Boolean {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun saveCurrentPhoto() {
        val values = ContentValues().apply {
            put(Images.Media.DATE_TAKEN, System.currentTimeMillis())
            put(Images.Media.MIME_TYPE, "image/jpeg")
            put(Images.ImageColumns.WIDTH, currentBitmap.width)
            put(Images.ImageColumns.HEIGHT, currentBitmap.height)
        }

        val uri = contentResolver.insert(
            Images.Media.EXTERNAL_CONTENT_URI, values
        ) ?: return

        contentResolver.openOutputStream(uri)?.use { output ->
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
        }
    }

    private fun requestWriteExternalPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0
        )
    }
}
