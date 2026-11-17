package com.smokingtracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.R
import com.smokingtracker.domain.statistics.BaselinePreviewData
import com.smokingtracker.domain.statistics.PackYearsRiskLevel
import com.smokingtracker.ui.theme.containerShape
import java.util.Locale

@Composable
fun BaselineRiskStatsCard(
    preview: BaselinePreviewData,
    currencySymbol: String,
    locale: Locale,
    modifier: Modifier = Modifier
) {
    val riskColor = when (preview.riskLevel) {
        PackYearsRiskLevel.LOW -> MaterialTheme.colorScheme.primary
        PackYearsRiskLevel.MODERATE -> MaterialTheme.colorScheme.tertiary
        PackYearsRiskLevel.HIGH -> MaterialTheme.colorScheme.error
    }
    val riskContainer = when (preview.riskLevel) {
        PackYearsRiskLevel.LOW -> MaterialTheme.colorScheme.primaryContainer
        PackYearsRiskLevel.MODERATE -> MaterialTheme.colorScheme.tertiaryContainer
        PackYearsRiskLevel.HIGH -> MaterialTheme.colorScheme.errorContainer
    }
    val riskOnContainer = when (preview.riskLevel) {
        PackYearsRiskLevel.LOW -> MaterialTheme.colorScheme.onPrimaryContainer
        PackYearsRiskLevel.MODERATE -> MaterialTheme.colorScheme.onTertiaryContainer
        PackYearsRiskLevel.HIGH -> MaterialTheme.colorScheme.onErrorContainer
    }
    val riskText = when (preview.riskLevel) {
        PackYearsRiskLevel.LOW -> stringResource(R.string.baseline_risk_low)
        PackYearsRiskLevel.MODERATE -> stringResource(R.string.baseline_risk_moderate)
        PackYearsRiskLevel.HIGH -> stringResource(R.string.baseline_risk_high)
    }
    val riskDesc = when (preview.riskLevel) {
        PackYearsRiskLevel.LOW -> stringResource(R.string.baseline_risk_low_desc)
        PackYearsRiskLevel.MODERATE -> stringResource(R.string.baseline_risk_moderate_desc)
        PackYearsRiskLevel.HIGH -> stringResource(R.string.baseline_risk_high_desc)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = containerShape(RoundedCornerShape(20.dp)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = riskColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.baseline_pack_years_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = riskContainer
                ) {
                    Text(
                        text = riskText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = riskOnContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.baseline_pack_years_value, preview.packYears),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = riskDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.history_preview_total_cigs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(
                            R.string.history_cigs_count_format,
                            String.format(locale, "%,d", preview.estimatedPastCigarettes)
                        ),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.history_preview_total_cost),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(locale, "%,.0f %s", preview.estimatedPastCost, currencySymbol),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
