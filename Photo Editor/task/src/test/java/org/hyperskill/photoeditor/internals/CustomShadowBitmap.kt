package org.hyperskill.photoeditor.internals

import android.graphics.Bitmap
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowLegacyBitmap
import java.io.OutputStream

// version 1.5
@Implements(Bitmap::class)
class CustomShadowBitmap : ShadowLegacyBitmap() {
    /**
     * These values won't be reset automatically for each @Test.
     *
     * Use init() for tests that use CustomShadowBitmap, if many tests use it consider calling this
     * on a setup method annotated with @Before or equivalent annotation
     */
    object LastCompressed {
        var compressedFormat : Bitmap.CompressFormat? = null
        var compressedBitmap : Bitmap? = null
        var compressedQuality : Int? = null
        var compressedStream : OutputStream? = null

        fun init() {
            compressedFormat = null
            compressedBitmap = null
            compressedQuality = null
            compressedStream = null
        }
    }

    @Implementation
    override fun compress(
        format: Bitmap.CompressFormat?,
        quality: Int,
        stream: OutputStream?
    ): Boolean {
        LastCompressed.compressedFormat = format
        LastCompressed.compressedBitmap = realBitmap
        LastCompressed.compressedQuality = quality
        LastCompressed.compressedStream = stream
        return super.compress(format, quality, stream)
    }
}