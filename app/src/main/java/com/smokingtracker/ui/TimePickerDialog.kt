package com.smokingtracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = com.smokingtracker.ui.theme.containerShape(androidx.compose.foundation.shape.RoundedCornerShape(28.dp)),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = com.smokingtracker.ui.theme.containerBorder(),
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                content()
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}

@Preview
@Composable
fun TimePickerDialogPreview() {
    MaterialTheme {
        TimePickerDialog(
            title = "Select Time",
            onDismissRequest = {},
            confirmButton = {
                TextButton(onClick = {}) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {}) {
                    Text("Cancel")
                }
            },
            content = {
                Text("Content goes here")
            }
        )
    }
}
