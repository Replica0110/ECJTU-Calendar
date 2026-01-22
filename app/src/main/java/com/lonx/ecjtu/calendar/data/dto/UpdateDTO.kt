package com.lonx.ecjtu.calendar.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateDTO(
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val size: Long,
)

@Serializable
data class GitHubReleaseDTO(
    val url: String?,
    @SerialName("tag_name")
    val tagName: String?,
    val name: String?,
    val body: String?,
    @SerialName("assets")
    val assetDTOS: List<AssetDTO>?
) {
    @Serializable
    data class AssetDTO(
        @SerialName("browser_download_url")
        val browserDownloadUrl: String?,
        val name: String?,
        val url: String?,
        val size: Long?
    )
}

@Serializable
data class OutputMetadataDTO(
    val version: Int,
    @SerialName("elements")
    val elementsDTO: List<ElementDTO>
) {
    @Serializable
    data class ElementDTO(
        val versionCode: Int,
        val versionName: String
    )
}