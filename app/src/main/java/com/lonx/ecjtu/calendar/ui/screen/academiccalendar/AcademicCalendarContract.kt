package com.lonx.ecjtu.calendar.ui.screen.academiccalendar

data class AcademicCalendarUiState(
    val isLoading: Boolean = false,
    val imageUrl: String? = null,
    val error: String? = null,
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val imageData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AcademicCalendarUiState

        if (isLoading != other.isLoading) return false
        if (scale != other.scale) return false
        if (offsetX != other.offsetX) return false
        if (offsetY != other.offsetY) return false
        if (imageUrl != other.imageUrl) return false
        if (error != other.error) return false
        if (!imageData.contentEquals(other.imageData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isLoading.hashCode()
        result = 31 * result + scale.hashCode()
        result = 31 * result + offsetX.hashCode()
        result = 31 * result + offsetY.hashCode()
        result = 31 * result + (imageUrl?.hashCode() ?: 0)
        result = 31 * result + (error?.hashCode() ?: 0)
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        return result
    }
}