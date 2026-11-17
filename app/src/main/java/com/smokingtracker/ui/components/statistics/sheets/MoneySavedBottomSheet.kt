package com.smokingtracker.ui.components.statistics.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.ui.components.MoneySavedSupportBanner
import com.smokingtracker.ui.components.SupportBottomSheet
import com.smokingtracker.ui.theme.containerShape
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneySavedBottomSheet(
    moneySaved: Float,
    currencySymbol: String,
    packPrice: Float,
    packSize: Int,
    dailyAvg: Float,
    dailyLimit: Int = 0,
    baselineDailyAvg: Int = 0,
    trackingDays: Int = 1,
    savingsGoalTitle: String = "",
    savingsGoalAmount: Float = 0f,
    onSaveSavingsGoal: (String, Float) -> Unit = { _, _ -> },
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pricePerCig = if (packSize > 0) packPrice / packSize.toFloat() else 0f
    val referenceDailyTarget = when {
        dailyLimit > 0 -> dailyLimit.toFloat()
        baselineDailyAvg > 0 -> baselineDailyAvg.toFloat()
        else -> 0f
    }
    val dailySavedRate = when {
        referenceDailyTarget > 0f -> (referenceDailyTarget - dailyAvg.coerceAtLeast(0f)).coerceAtLeast(0f) * pricePerCig
        trackingDays > 0 && moneySaved > 0f -> moneySaved / trackingDays.coerceAtLeast(1).toFloat()
        else -> 0f
    }
    val monthlyProj = dailySavedRate * 30f
    val halfYearProj = dailySavedRate * 182.5f
    val yearProj = dailySavedRate * 365f

    var goalName by rememberSaveable(savingsGoalTitle) { mutableStateOf(savingsGoalTitle) }
    var goalAmountStr by rememberSaveable(savingsGoalAmount) {
        mutableStateOf(if (savingsGoalAmount > 0f) String.format(Locale.getDefault(), "%.0f", savingsGoalAmount) else "")
    }
    var showSupportSheet by remember { mutableStateOf(false) }

    val hasGoal = savingsGoalAmount > 0f && savingsGoalTitle.isNotBlank()
    val goalProgress = if (savingsGoalAmount > 0f) (moneySaved / savingsGoalAmount).coerceIn(0f, 1f) else 0f
    val goalPercent = (goalProgress * 100).toInt()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = containerShape(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        containerColor = if (MaterialTheme.colorScheme.surfaceContainerLow == Color.White) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        val locale = LocalLocale.current.platformLocale
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.stats_sheet_money_title),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.btn_close))
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${String.format(locale, "%.2f", moneySaved)} $currencySymbol",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.stats_sheet_money_current),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Flag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.stats_financial_goal_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (hasGoal) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = stringResource(R.string.stats_goal_progress, goalPercent),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    if (hasGoal) {
                        LinearWavyProgressIndicator(
                            progress = { goalProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
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
                        trailingIcon = { Text(currencySymbol, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(end = 12.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    val parsedAmount = goalAmountStr.replace(',', '.').toFloatOrNull() ?: 0f
                    val isGoalValid = goalName.isNotBlank() && parsedAmount.isFinite() && parsedAmount > 0f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isGoalValid) {
                                    onSaveSavingsGoal(goalName.trim(), parsedAmount)
                                }
                            },
                            enabled = isGoalValid,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.stats_save_goal))
                        }

                        if (hasGoal) {
                            OutlinedButton(
                                onClick = {
                                    goalName = ""
                                    goalAmountStr = ""
                                    onSaveSavingsGoal("", 0f)
                                },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.stats_clear_goal))
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.stats_sheet_money_projections_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.stats_sheet_money_month, String.format(locale, "%.1f %s", monthlyProj, currencySymbol)),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        Text(
                            text = stringResource(R.string.stats_sheet_money_half_year, String.format(locale, "%.1f %s", halfYearProj, currencySymbol)),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        Text(
                            text = stringResource(R.string.stats_sheet_money_year, String.format(locale, "%.1f %s", yearProj, currencySymbol)),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.stats_sheet_money_tip_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.stats_sheet_money_tip_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (moneySaved > 0f) {
                val formattedSaved = "${String.format(locale, "%.2f", moneySaved)} $currencySymbol"
                MoneySavedSupportBanner(
                    moneySavedFormatted = formattedSaved,
                    onTreatClick = { showSupportSheet = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSupportSheet) {
        SupportBottomSheet(
            onDismissRequest = { showSupportSheet = false }
        )
    }
}
