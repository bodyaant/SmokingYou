package com.smokingtracker.data.manager

import android.content.Context
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.smokingtracker.BuildConfig
import com.smokingtracker.UpdateCheckState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class GitHubUpdateManager(
    private val gson: Gson = Gson(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    @Suppress("unused")
    constructor(
        context: Context,
        gson: Gson = Gson(),
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ) : this(gson = gson, ioDispatcher = ioDispatcher)

    private val _updateCheckState = MutableStateFlow<UpdateCheckState>(UpdateCheckState.Idle)
    val updateCheckState: StateFlow<UpdateCheckState> = _updateCheckState.asStateFlow()

    private val checkMutex = Mutex()

    suspend fun checkForUpdatesWithState(isManual: Boolean) {
        if (!checkMutex.tryLock()) return
        try {
            if (isManual) _updateCheckState.value = UpdateCheckState.Checking
            _updateCheckState.value = when (val result = checkForUpdates()) {
                is UpdateResult.NewUpdate -> UpdateCheckState.NewUpdate(result.release)
                is UpdateResult.NoUpdate  -> if (isManual) UpdateCheckState.NoUpdate else UpdateCheckState.Idle
                is UpdateResult.Error     -> if (isManual) UpdateCheckState.Error(result.message) else UpdateCheckState.Idle
            }
        } finally {
            checkMutex.unlock()
        }
    }

    fun resetUpdateCheckState() {
        _updateCheckState.value = UpdateCheckState.Idle
    }

    suspend fun checkForUpdates(): UpdateResult = withContext(ioDispatcher) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("https://api.github.com/repos/bodyaant/SmokingYou/releases/latest")
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "SmokingYou-Android-App")
                connectTimeout = 8000
                readTimeout = 8000
            }

            val responseCode = connection.responseCode
            when (responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val release = connection.inputStream.use { stream ->
                        BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                            gson.fromJson(reader, GitHubRelease::class.java)
                        }
                    }

                    if (release != null) {
                        val currentVersion = BuildConfig.VERSION_NAME
                        val latestVersion = release.tagName

                        if (latestVersion != null && isNewerVersion(latestVersion, currentVersion)) {
                            UpdateResult.NewUpdate(release)
                        } else {
                            UpdateResult.NoUpdate
                        }
                    } else {
                        UpdateResult.Error("Empty response body")
                    }
                }
                HttpURLConnection.HTTP_FORBIDDEN, 429 -> {
                    val remaining = connection.getHeaderField("X-RateLimit-Remaining")?.trim()
                    if (responseCode == 429 || remaining == "0") {
                        UpdateResult.Error("GitHub API rate limit exceeded. Please try again later.")
                    } else {
                        UpdateResult.Error("GitHub API access forbidden (HTTP $responseCode)")
                    }
                }
                else -> {
                    UpdateResult.Error("API error code: $responseCode")
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            UpdateResult.Error(e.localizedMessage ?: "Network connection error")
        } finally {
            connection?.disconnect()
        }
    }

    internal fun isNewerVersion(latest: String, current: String): Boolean {
        val latestSemVer = parseSemVer(latest) ?: return false
        val currentSemVer = parseSemVer(current) ?: return false
        return latestSemVer > currentSemVer
    }

    private data class SemVer(
        val core: List<Int>,
        val preRelease: String?
    ) : Comparable<SemVer> {
        override fun compareTo(other: SemVer): Int {
            val maxLen = maxOf(core.size, other.core.size)
            for (i in 0 until maxLen) {
                val a = core.getOrElse(i) { 0 }
                val b = other.core.getOrElse(i) { 0 }
                if (a != b) return a.compareTo(b)
            }
            if (preRelease == null && other.preRelease == null) return 0
            if (preRelease == null && other.preRelease != null) return 1
            if (preRelease != null && other.preRelease == null) return -1

            val aIdentifiers = preRelease!!.split(".")
            val bIdentifiers = other.preRelease!!.split(".")
            val preLen = minOf(aIdentifiers.size, bIdentifiers.size)
            for (i in 0 until preLen) {
                val aId = aIdentifiers[i]
                val bId = bIdentifiers[i]
                val aNum = aId.toLongOrNull()
                val bNum = bId.toLongOrNull()
                val cmp = when {
                    aNum != null && bNum != null -> aNum.compareTo(bNum)
                    aNum != null && bNum == null -> -1 // Numeric identifiers have lower precedence than non-numeric
                    aNum == null && bNum != null -> 1
                    else -> aId.compareTo(bId, ignoreCase = true)
                }
                if (cmp != 0) return cmp
            }
            return aIdentifiers.size.compareTo(bIdentifiers.size)
        }
    }

    private fun parseSemVer(rawVersion: String): SemVer? {
        var cleaned = rawVersion.trim()
        if (cleaned.isEmpty()) return null
        while (cleaned.startsWith("v", ignoreCase = true)) {
            cleaned = cleaned.substring(1).trim()
        }
        if (cleaned.isEmpty()) return null

        val withoutBuildMeta = cleaned.substringBefore('+')
        val corePart = withoutBuildMeta.substringBefore('-').trim()
        val preReleasePart = if ('-' in withoutBuildMeta) {
            withoutBuildMeta.substringAfter('-').trim().takeIf { it.isNotEmpty() }
        } else {
            null
        }

        if (corePart.isEmpty()) return null
        val segments = corePart.split('.')
        val parsedSegments = ArrayList<Int>(segments.size)
        for (segment in segments) {
            val trimmedSeg = segment.trim()
            if (trimmedSeg.isEmpty()) return null
            val numPrefix = trimmedSeg.takeWhile { it.isDigit() }
            val num = numPrefix.toIntOrNull() ?: return null
            parsedSegments.add(num)
        }
        return if (parsedSegments.isNotEmpty()) SemVer(parsedSegments, preReleasePart) else null
    }

    sealed class UpdateResult {
        data class NewUpdate(val release: GitHubRelease) : UpdateResult()
        data object NoUpdate : UpdateResult()
        data class Error(val message: String) : UpdateResult()
    }
}

@Keep
data class GitHubRelease(
    @SerializedName("tag_name") val tagName: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("body") val body: String?,
    @SerializedName("html_url") val htmlUrl: String?,
    @SerializedName("published_at") val publishedAt: String? = null,
    @SerializedName("assets") val assets: List<GitHubAsset>? = emptyList()
)

@Keep
data class GitHubAsset(
    @SerializedName("name") val name: String?,
    @SerializedName("browser_download_url") val browserDownloadUrl: String?,
    @SerializedName("size") val size: Long?
)
