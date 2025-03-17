package org.hyperskill.photoeditor

data class FilterSettings(
    val brightness: Float,
    val contrast: Float,
    val saturation: Float,
    val gamma: Float
) {
    companion object {
        val default = FilterSettings(
            brightness = 0f,
            contrast = 0f,
            saturation = 0f,
            gamma = 1f
        )
    }
}
