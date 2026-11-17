package com.smokingtracker.ui.home.dialogs

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.containerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDatePickerDialog(
    visible: Boolean,
    currentDateMillis: Long,
    vibrationEnabled: Boolean = true,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        shape = containerShape(RoundedCornerShape(28.dp)),
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        confirmButton = {
            TextButton(
                onClick = {
                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(millis.coerceAtMost(System.currentTimeMillis()))
                    }
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.dialog_ok), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel), fontWeight = FontWeight.Bold)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
