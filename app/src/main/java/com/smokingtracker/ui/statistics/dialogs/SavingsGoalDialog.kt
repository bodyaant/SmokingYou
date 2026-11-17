package com.smokingtracker.ui.statistics.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SavingsGoalDialog(
    savingsGoalTitle: String = "",
    savingsGoalAmount: Float = 0f,
    currencySymbol: String = "",
    currentSaved: Float = 0f,
    onSaveGoal: (title: String, amount: Float) -> Unit,
    onDismissRequest: () -> Unit
) {
    var goalName by rememberSaveable(savingsGoalTitle) { mutableStateOf(savingsGoalTitle) }
    var goalAmountStr by rememberSaveable(savingsGoalAmount) {
        mutableStateOf(if (savingsGoalAmount > 0f) String.format(Locale.getDefault(), "%.0f", savingsGoalAmount) else "")
    }

    val hasGoal = savingsGoalAmount > 0f && savingsGoalTitle.isNotBlank()
    val goalProgress = if (savingsGoalAmount > 0f) (currentSaved / savingsGoalAmount).coerceIn(0f, 1f) else 0f
    val goalPercent = (goalProgress * 100).toInt()

    val parsedAmount = goalAmountStr.replace(',', '.').toFloatOrNull() ?: 0f
    val isGoalValid = goalName.isNotBlank() && parsedAmount.isFinite() && parsedAmount > 0f

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Flag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.stats_financial_goal_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (hasGoal) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = savingsGoalTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.stats_goal_progress, goalPercent),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        LinearWavyProgressIndicator(
                            progress = { goalProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                OutlinedTextField(
                    value = goalName,
                    onValueChange = { if (it.length <= 60) goalName = it },
                    label = { Text(stringResource(R.string.stats_goal_name_label)) },
                    placeholder = { Text(stringResource(R.string.stats_goal_name_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = goalAmountStr,
                    onValueChange = { input ->
                        val normalized = input.replace(',', '.')
                        if (normalized.length <= 10 && normalized.count { it == '.' } <= 1 && normalized.all { it.isDigit() || it == '.' }) {
                            goalAmountStr = input
                        }
                    },
                    label = { Text(stringResource(R.string.stats_goal_amount_label)) },
                    trailingIcon = {
                        if (currencySymbol.isNotBlank()) {
                            Text(
                                text = currencySymbol,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isGoalValid) {
                        onSaveGoal(goalName.trim(), parsedAmount)
                        onDismissRequest()
                    }
                },
                enabled = isGoalValid,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.stats_save_goal))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (hasGoal) {
                    OutlinedButton(
                        onClick = {
                            onSaveGoal("", 0f)
                            onDismissRequest()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.stats_clear_goal))
                    }
                }
                TextButton(
                    onClick = onDismissRequest,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        }
    )
}
