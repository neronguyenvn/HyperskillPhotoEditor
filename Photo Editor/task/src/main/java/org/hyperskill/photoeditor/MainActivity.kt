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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.hyperskill.photoeditor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val imageView by lazy {
        binding.ivPhoto
    }

    private lateinit var imageProcessor: ImageProcessor

    private val imageResultLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { imageUri ->
                imageProcessor.setImage(imageUri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val bitmap = createBitmap()
        imageProcessor = ImageProcessor(this, bitmap)

        setupButtonListeners()
        setupSliderListeners()

        imageProcessor.processedImage.onEach {
            imageView.setImageBitmap(it)
        }.launchIn(lifecycleScope)
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

        var r: Int
        var g: Int
        var b: Int
        var index: Int

        for (y in 0 until height) {
            for (x in 0 until width) {

                // get current index in 2D-matrix
                index = y * width + x

                // get color
                r = x % 100 + 40
                g = y % 100 + 80
                b = (x + y) % 100 + 120

                pixels[index] = Color.rgb(r, g, b)
            }
        }

        // output bitmap
        val bitmapOut = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmapOut.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmapOut
    }

    private fun setupButtonListeners() = with(binding) {
        btnGallery.setOnClickListener {
            val selectImageIntent = Intent(
                Intent.ACTION_PICK,
                Images.Media.EXTERNAL_CONTENT_URI
            )
            imageResultLauncher.launch(selectImageIntent)
        }

        btnSave.setOnClickListener {
            if (hasWriteExternalPermission()) {
                saveCurrentPhoto()
            } else {
                requestWriteExternalPermission()
            }
        }
    }

    private fun setupSliderListeners() = with(binding) {
        slBrightness.addOnChangeListener { _, value, _ ->
            imageProcessor.changeBrightness(value)
        }

        slContrast.addOnChangeListener { _, value, _ ->
            imageProcessor.changeContrast(value)
        }

        slSaturation.addOnChangeListener { _, value, _ ->
            imageProcessor.changeSaturation(value)
        }

        slGamma.addOnChangeListener { _, value, _ ->
            imageProcessor.changeGamma(value)
        }
    }


    private fun hasWriteExternalPermission(): Boolean {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun saveCurrentPhoto() {
        lifecycleScope.launch {
            imageProcessor.processedImage.first().let { currentBitmap ->
                val values = ContentValues().apply {
                    put(Images.Media.DATE_TAKEN, System.currentTimeMillis())
                    put(Images.Media.MIME_TYPE, "image/jpeg")
                    put(Images.ImageColumns.WIDTH, currentBitmap.width)
                    put(Images.ImageColumns.HEIGHT, currentBitmap.height)
                }

                val uri = contentResolver.insert(
                    Images.Media.EXTERNAL_CONTENT_URI, values
                ) ?: return@let

                contentResolver.openOutputStream(uri)?.use { output ->
                    currentBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
                }
            }
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
