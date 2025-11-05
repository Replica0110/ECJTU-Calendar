package com.lonx.ecjtu.calendar.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateDTO(
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String
)
@Serializable
data class GitHubReleaseDTO(
    val tag_name: String?,
    val body: String?,
    val assetDTOS: List<AssetDTO>?
) {
    @Serializable
    data class AssetDTO(
        val browser_download_url: String?,
        val name: String?
    )
}
@Serializable
data class OutputMetadataDTO(
    val version: Int,
    val elementDTOS: List<ElementDTO>
) {
    @Serializable
    data class ElementDTO(
        val versionCode: Int,
        val versionName: String
    )
}