package com.lonx.ecjtu.calendar.data.dto

import com.google.gson.annotations.SerializedName
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
    @SerializedName("tag_name")
    val tagName: String?,
    val name: String?,
    val body: String?,
    @SerializedName("assets")
    val assetDTOS: List<AssetDTO>?
) {
    @Serializable
    data class AssetDTO(
        @SerializedName("browser_download_url")
        val browserDownloadUrl: String?,
        val name: String?,
        val url: String?,
        val size: Long?
    )
}

@Serializable
data class OutputMetadataDTO(
    val version: Int,
    @SerializedName("elements")
    val elementsDTO: List<ElementDTO>
) {
    @Serializable
    data class ElementDTO(
        val versionCode: Int,
        val versionName: String
    )
}