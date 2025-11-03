package com.lonx.ecjtu.calendar.data.dto

data class UpdateDTO(
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String
)

data class GitHubReleaseDTO(
    val tag_name: String?,
    val body: String?,
    val assetDTOS: List<AssetDTO>?
) {
    data class AssetDTO(
        val browser_download_url: String?,
        val name: String?
    )
}

data class OutputMetadataDTO(
    val version: Int,
    val elementDTOS: List<ElementDTO>
) {
    data class ElementDTO(
        val versionCode: Int,
        val versionName: String
    )
}