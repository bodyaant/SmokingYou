package com.smokingtracker.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smokingtracker.R

enum class BackupRestoreState {
    CHOOSE_FILE,
    LOADING,
    DONE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupBottomSheet(
    onDismissRequest: () -> Unit,
    onBackupData: (Uri, onSuccess: () -> Unit, onError: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var backupState by remember { mutableStateOf(BackupRestoreState.CHOOSE_FILE) }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            selectedUri = it
            backupState = BackupRestoreState.CHOOSE_FILE
        }
    }

    val selectedFileName = remember(selectedUri) {
        selectedUri?.getFileName(context)
    }

    BackupRestoreBottomSheetTemplate(
        state = backupState,
        onDismissRequest = onDismissRequest,
        openPicker = { backupLauncher.launch("smoking_tracker_backup.json") },
        onStartAction = {
            selectedUri?.let { uri ->
                backupState = BackupRestoreState.LOADING
                onBackupData(
                    uri,
                    { backupState = BackupRestoreState.DONE },
                    { backupState = BackupRestoreState.CHOOSE_FILE }
                )
            }
        },
        headerIcon = Icons.Filled.Backup,
        titleText = stringResource(R.string.backup_dialog_title),
        descriptionText = stringResource(R.string.backup_dialog_desc),
        fileSelectorPrompt = stringResource(R.string.backup_choose_file_location),
        actionButtonText = if (backupState == BackupRestoreState.DONE) {
            stringResource(R.string.backup_restore_close)
        } else if (selectedUri == null) {
            stringResource(R.string.backup_choose_file_location)
        } else {
            stringResource(R.string.backup_start_action)
        },
        selectedFileName = selectedFileName,
        pickerIcon = Icons.Filled.Folder
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreBottomSheet(
    onDismissRequest: () -> Unit,
    onRestoreData: (Uri, onSuccess: () -> Unit, onError: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var restoreState by remember { mutableStateOf(BackupRestoreState.CHOOSE_FILE) }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            selectedUri = it
            restoreState = BackupRestoreState.CHOOSE_FILE
        }
    }

    val selectedFileName = remember(selectedUri) {
        selectedUri?.getFileName(context)
    }

    BackupRestoreBottomSheetTemplate(
        state = restoreState,
        onDismissRequest = onDismissRequest,
        openPicker = { restoreLauncher.launch(arrayOf("application/json")) },
        onStartAction = {
            selectedUri?.let { uri ->
                restoreState = BackupRestoreState.LOADING
                onRestoreData(
                    uri,
                    { restoreState = BackupRestoreState.DONE },
                    { restoreState = BackupRestoreState.CHOOSE_FILE }
                )
            }
        },
        headerIcon = Icons.Filled.Restore,
        titleText = stringResource(R.string.restore_dialog_title),
        descriptionText = stringResource(R.string.restore_dialog_desc),
        fileSelectorPrompt = stringResource(R.string.restore_choose_file),
        actionButtonText = if (restoreState == BackupRestoreState.DONE) {
            stringResource(R.string.backup_restore_close)
        } else if (selectedUri == null) {
            stringResource(R.string.restore_choose_file)
        } else {
            stringResource(R.string.restore_start_action)
        },
        selectedFileName = selectedFileName,
        pickerIcon = Icons.AutoMirrored.Filled.InsertDriveFile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackupRestoreBottomSheetTemplate(
    state: BackupRestoreState,
    onDismissRequest: () -> Unit,
    openPicker: () -> Unit,
    onStartAction: () -> Unit,
    headerIcon: ImageVector,
    titleText: String,
    descriptionText: String,
    fileSelectorPrompt: String,
    actionButtonText: String,
    selectedFileName: String?,
    pickerIcon: ImageVector
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val animatedCardBg by animateColorAsState(
        targetValue = when (state) {
            BackupRestoreState.DONE -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainerHighest
        },
        label = "cardBgColor"
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 8.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = headerIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = titleText,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = descriptionText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(animatedCardBg)
                    .clickable(
                        enabled = state == BackupRestoreState.CHOOSE_FILE,
                        onClick = openPicker
                    )
                    .padding(16.dp)
            ) {
                AnimatedContent(
                    targetState = state,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "iconTransition"
                ) { targetState ->
                    when (targetState) {
                        BackupRestoreState.CHOOSE_FILE -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = pickerIcon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        BackupRestoreState.LOADING -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(48.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp
                                )
                            }
                        }
                        BackupRestoreState.DONE -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = selectedFileName ?: fileSelectorPrompt,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (selectedFileName != null) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = if (selectedFileName != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state != BackupRestoreState.DONE) {
                    TextButton(
                        onClick = onDismissRequest,
                        enabled = state != BackupRestoreState.LOADING,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(stringResource(R.string.backup_restore_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Button(
                    onClick = {
                        if (state == BackupRestoreState.DONE) {
                            onDismissRequest()
                        } else if (selectedFileName == null) {
                            openPicker()
                        } else {
                            onStartAction()
                        }
                    },
                    enabled = state != BackupRestoreState.LOADING,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    AnimatedContent(
                        targetState = actionButtonText,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "btnTextTransition"
                    ) { text ->
                        Text(text)
                    }
                }
            }
        }
    }
}

private fun Uri.getFileName(context: Context): String {
    var fileName: String? = null
    if (this.scheme == "content") {
        context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
    }
    if (fileName == null) {
        fileName = this.path?.substringAfterLast('/')
    }
    return fileName ?: "smoking_tracker_backup.json"
}
