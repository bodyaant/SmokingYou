package com.smokingtracker.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.AchievementsManager
import com.smokingtracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    unlockedAchievements: Set<String>,
    achievementsManager: AchievementsManager,
    onNavigateToAchievements: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        actions = {
            Surface(
                onClick = onNavigateToAchievements,
                shape = RoundedCornerShape(100),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = stringResource(R.string.settings_achievements),
                        modifier = Modifier.size(18.dp)
                    )
                    val allAchIds = remember(achievementsManager) {
                        achievementsManager.achievementsList.map { it.id }.toSet()
                    }
                    val validUnlockedCount = remember(unlockedAchievements, allAchIds) {
                        unlockedAchievements.count { it in allAchIds }
                    }
                    val totalAchievementsCount = allAchIds.size
                    Text(
                        text = "$validUnlockedCount/$totalAchievementsCount",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}
