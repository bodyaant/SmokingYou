package com.smokingtracker

import com.smokingtracker.data.manager.GitHubRelease

sealed interface UpdateCheckState {
    data object Idle     : UpdateCheckState
    data object Checking : UpdateCheckState
    data class  NewUpdate(val release: GitHubRelease) : UpdateCheckState
    data object NoUpdate : UpdateCheckState
    data class  Error(val message: String) : UpdateCheckState
}
