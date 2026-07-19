package com.smokingtracker.data.manager

import android.content.Context
import com.google.gson.Gson
import com.smokingtracker.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GitHubUpdateManager(private val context: Context) {

    private val gson = Gson()

    suspend fun checkForUpdates(): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/mem2sp/SmokingYou/releases/latest")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("User-Agent", "SmokingYou-Android-App")
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = InputStreamReader(connection.inputStream)
                val release = gson.fromJson(reader, GitHubRelease::class.java)
                reader.close()

                if (release != null) {
                    val currentVersion = BuildConfig.VERSION_NAME
                    val latestVersion = release.tagName

                    if (isNewerVersion(latestVersion, currentVersion)) {
                        UpdateResult.NewUpdate(release)
                    } else {
                        UpdateResult.NoUpdate
                    }
                } else {
                    UpdateResult.Error("Empty response body")
                }
            } else {
                UpdateResult.Error("API error code: $responseCode")
            }
        } catch (e: Exception) {
            UpdateResult.Error(e.localizedMessage ?: "Network connection error")
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestClean = latest.removePrefix("v").trim()
        val currentClean = current.removePrefix("v").trim()

        val latestParts = latestClean.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = currentClean.split(".").mapNotNull { it.toIntOrNull() }

        val maxLength = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLength) {
            val latestPart = latestParts.getOrElse(i) { 0 }
            val currentPart = currentParts.getOrElse(i) { 0 }
            if (latestPart > currentPart) return true
            if (latestPart < currentPart) return false
        }
        return false
    }

    sealed class UpdateResult {
        data class NewUpdate(val release: GitHubRelease) : UpdateResult()
        object NoUpdate : UpdateResult()
        data class Error(val message: String) : UpdateResult()
    }
}

data class GitHubRelease(
    @com.google.gson.annotations.SerializedName("tag_name") val tagName: String,
    val name: String,
    val body: String,
    @com.google.gson.annotations.SerializedName("html_url") val htmlUrl: String,
    @com.google.gson.annotations.SerializedName("published_at") val publishedAt: String? = null,
    val assets: List<GitHubAsset> = emptyList()
)

data class GitHubAsset(
    val name: String,
    @com.google.gson.annotations.SerializedName("browser_download_url") val browserDownloadUrl: String,
    val size: Long
)
