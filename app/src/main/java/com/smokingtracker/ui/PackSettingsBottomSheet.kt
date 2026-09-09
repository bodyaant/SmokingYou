package com.smokingtracker.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smokingtracker.R
import com.smokingtracker.ui.theme.HapticFeedbackHelper
import com.smokingtracker.ui.theme.containerBorder
import com.smokingtracker.ui.theme.containerShape
import java.util.Locale

private data class CurrencyInfo(val code: String, val symbol: String)

private val AVAILABLE_CURRENCIES = listOf(
    CurrencyInfo("RUB", "₽"),
    CurrencyInfo("USD", "$"),
    CurrencyInfo("EUR", "€"),
    CurrencyInfo("KZT", "₸"),
    CurrencyInfo("TRY", "₺"),
    CurrencyInfo("UAH", "₴"),
    CurrencyInfo("GBP", "£"),
    CurrencyInfo("BYN", "Br")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackSettingsBottomSheet(
    packPrice: Float,
    packSize: Int,
    currency: String,
    dailyLimit: Int = 0,
    onUpdatePackDetails: (Float, Int, String) -> Unit,
    onDismissRequest: () -> Unit,
    vibrationEnabled: Boolean = true
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val initialPriceStr = remember(packPrice) {
        if (packPrice > 0f) {
            if (packPrice % 1f == 0f) packPrice.toInt().toString() else packPrice.toString()
        } else ""
    }

    var priceInput by remember { mutableStateOf(initialPriceStr) }
    var selectedCurrency by remember { mutableStateOf(currency) }

    val standardSizes = listOf(20, 25, 30)
    var isCustomSize by remember { mutableStateOf(packSize !in standardSizes) }
    var selectedStandardSize by remember { mutableIntStateOf(if (packSize in standardSizes) packSize else 20) }
    var customSizeInput by remember { mutableStateOf(if (packSize !in standardSizes) packSize.toString() else "") }

    val priceVal = priceInput.toFloatOrNull() ?: 0f
    val currentSizeVal = if (isCustomSize) {
        customSizeInput.toIntOrNull() ?: 0
    } else {
        selectedStandardSize
    }

    val priceValid = priceInput.isEmpty() || (priceInput.toFloatOrNull() != null && priceVal >= 0f)
    val sizeValid = currentSizeVal > 0

    val pricePerCig = if (sizeValid && priceVal > 0f) priceVal / currentSizeVal else 0f
    val dailyCost = if (dailyLimit > 0) pricePerCig * dailyLimit else 0f
    val monthlyCost = dailyCost * 30f

    val currentCurrencySymbol = remember(selectedCurrency) {
        AVAILABLE_CURRENCIES.find { it.code == selectedCurrency }?.symbol ?: selectedCurrency
    }

    val savedStr = stringResource(R.string.pack_settings_saved)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = if (MaterialTheme.colorScheme.surfaceContainerLow == Color.White) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_pack_params),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.pack_settings_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = containerShape(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                border = containerBorder(
                    1.dp,
                    if (priceVal > 0f && sizeValid) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.pack_cost_summary_title),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (priceVal > 0f && sizeValid) {
                            val formattedPricePerCig = String.format(Locale.getDefault(), "%.2f", pricePerCig)
                            Text(
                                text = stringResource(R.string.pack_price_per_piece_format, formattedPricePerCig, currentCurrencySymbol),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (priceVal > 0f && sizeValid) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            thickness = 0.8.dp
                        )
                        if (dailyLimit > 0) {
                            val formattedDayCost = String.format(Locale.getDefault(), "%.1f", dailyCost)
                            val formattedMonthCost = String.format(Locale.getDefault(), "%.0f", monthlyCost)
                            Text(
                                text = stringResource(
                                    R.string.pack_daily_monthly_cost,
                                    dailyLimit,
                                    formattedDayCost,
                                    currentCurrencySymbol,
                                    formattedMonthCost
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.pack_per_cigarette_cost, String.format(Locale.getDefault(), "%.2f", pricePerCig), currentCurrencySymbol),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.settings_tap_configure),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            OutlinedTextField(
                value = priceInput,
                onValueChange = { input ->
                    priceInput = input.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.settings_pack_price)) },
                leadingIcon = {
                    Text(
                        text = currentCurrencySymbol,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                    )
                },
                trailingIcon = {
                    if (priceInput.isNotEmpty()) {
                        IconButton(onClick = { priceInput = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                isError = !priceValid,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                supportingText = if (!priceValid) {
                    { Text(stringResource(R.string.error_invalid_price)) }
                } else null
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.pack_size_presets),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    standardSizes.forEach { size ->
                        val isSelected = !isCustomSize && selectedStandardSize == size
                        Surface(
                            onClick = {
                                HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                isCustomSize = false
                                selectedStandardSize = size
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = containerShape(RoundedCornerShape(12.dp)),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            border = containerBorder(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = size.toString(),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = {
                            HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                            isCustomSize = true
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(42.dp),
                        shape = containerShape(RoundedCornerShape(12.dp)),
                        color = if (isCustomSize) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = if (isCustomSize) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        border = containerBorder(1.dp, if (isCustomSize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(R.string.pack_size_custom),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (isCustomSize) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = isCustomSize,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    OutlinedTextField(
                        value = customSizeInput,
                        onValueChange = { customSizeInput = it.filter { c -> c.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        label = { Text(stringResource(R.string.settings_pack_size)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Numbers, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = !sizeValid,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.settings_currency),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AVAILABLE_CURRENCIES) { curr ->
                        val isSelected = selectedCurrency == curr.code
                        Surface(
                            onClick = {
                                HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                                selectedCurrency = curr.code
                            },
                            modifier = Modifier.height(40.dp),
                            shape = containerShape(RoundedCornerShape(12.dp)),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            border = containerBorder(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${curr.symbol} ${curr.code}",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                    val price = priceInput.toFloatOrNull() ?: 0.0f
                    val size = if (currentSizeVal > 0) currentSizeVal else 20
                    onUpdatePackDetails(price, size, selectedCurrency)
                    Toast.makeText(context, savedStr, Toast.LENGTH_SHORT).show()
                    onDismissRequest()
                },
                enabled = priceValid && sizeValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(R.string.pack_save_button),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
