package com.smokingtracker.ui.home.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.ui.graphics.vector.ImageVector

data class AppSnackbarVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    val icon: ImageVector = Icons.Filled.Shield,
    val isError: Boolean = false
) : SnackbarVisuals
