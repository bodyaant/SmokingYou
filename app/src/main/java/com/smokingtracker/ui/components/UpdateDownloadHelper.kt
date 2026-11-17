package com.smokingtracker.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import com.smokingtracker.R
import com.smokingtracker.data.manager.GitHubAsset
import com.smokingtracker.data.manager.GitHubRelease

object UpdateDownloadHelper {

    fun findApkAsset(release: GitHubRelease): GitHubAsset? {
        return release.assets?.firstOrNull { it.name?.endsWith(".apk", ignoreCase = true) == true }
    }

    fun getDownloadUrl(release: GitHubRelease): String? {
        val apkAsset = findApkAsset(release)
        return apkAsset?.browserDownloadUrl ?: release.htmlUrl
    }

    fun openUpdateDownload(context: Context, release: GitHubRelease): Boolean {
        val downloadUrl = getDownloadUrl(release)
        return openUrl(context = context, url = downloadUrl)
    }

    fun openUrl(context: Context, url: String?): Boolean {
        return if (!url.isNullOrEmpty()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    context.getString(R.string.update_error, e.localizedMessage ?: url),
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.update_error, "No URL"),
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }
}
