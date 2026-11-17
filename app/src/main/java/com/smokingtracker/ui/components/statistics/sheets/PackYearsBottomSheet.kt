package com.smokingtracker.ui.components.statistics.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.StatisticsManager
import com.smokingtracker.ui.theme.containerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackYearsBottomSheet(
    packYears: Float,
    riskLevel: StatisticsManager.PackYearsRiskLevel,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val (riskColor, riskTitleRes, riskDescRes) = when (riskLevel) {
        StatisticsManager.PackYearsRiskLevel.LOW -> Triple(
            MaterialTheme.colorScheme.primary,
            R.string.baseline_risk_low,
            R.string.baseline_risk_low_desc
        )
        StatisticsManager.PackYearsRiskLevel.MODERATE -> Triple(
            MaterialTheme.colorScheme.tertiary,
            R.string.baseline_risk_moderate,
            R.string.baseline_risk_moderate_desc
        )
        StatisticsManager.PackYearsRiskLevel.HIGH -> Triple(
            MaterialTheme.colorScheme.error,
            R.string.baseline_risk_high,
            R.string.baseline_risk_high_desc
        )
    }

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
                    text = stringResource(R.string.stats_sheet_pack_years_title),
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
                        text = String.format(locale, "%.1f", packYears),
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                        color = riskColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = CircleShape,
                        color = riskColor.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = stringResource(riskTitleRes),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = riskColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.stats_sheet_pack_years_calc_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.stats_sheet_pack_years_calc_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(riskDescRes),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
