package com.lonx.ecjtu.calendar.data.model

data class UpdateInfo(
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String
)

data class GitHubRelease(
    val tag_name: String?,
    val body: String?,
    val assets: List<Asset>?
) {
    data class Asset(
        val browser_download_url: String?,
        val name: String?
    )
}

data class OutputMetadata(
    val version: Int,
    val elements: List<Element>
) {
    data class Element(
        val versionCode: Int,
        val versionName: String
    )
}