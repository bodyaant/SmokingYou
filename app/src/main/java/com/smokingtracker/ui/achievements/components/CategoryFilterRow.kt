package com.smokingtracker.ui.achievements.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smokingtracker.AchievementCategory
import com.smokingtracker.AchievementData
import com.smokingtracker.R
import com.smokingtracker.ui.theme.bouncyPress
import com.smokingtracker.ui.theme.rememberBouncyPress

@Composable
fun CategoryFilterRow(
    allAchievements: List<AchievementData>,
    unlockedAchievements: Set<String>,
    selectedCategory: AchievementCategory?,
    onSelectCategory: (AchievementCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = remember {
        listOf(
            null,
            AchievementCategory.NO_SMOKE,
            AchievementCategory.LOGIN,
            AchievementCategory.SECRET
        )
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(categories, key = { it?.name ?: "ALL" }) { category ->
            val isSelected = selectedCategory == category
            val title = if (category == null) {
                stringResource(R.string.ach_category_all)
            } else {
                stringResource(category.titleResId)
            }

            val matchingAchievements = remember(category, allAchievements) {
                if (category == null) allAchievements else allAchievements.filter { it.category == category }
            }
            val countUnlocked = remember(matchingAchievements, unlockedAchievements) {
                matchingAchievements.count { unlockedAchievements.contains(it.id) }
            }

            val interactionSource = remember { MutableInteractionSource() }
            val bouncyState = rememberBouncyPress(interactionSource = interactionSource, targetScale = 0.86f)

            FilterChip(
                selected = isSelected,
                onClick = {
                    bouncyState.bounce()
                    onSelectCategory(category)
                },
                interactionSource = interactionSource,
                label = {
                    Text(
                        text = "$title ($countUnlocked/${matchingAchievements.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = null,
                shape = RoundedCornerShape(100),
                modifier = Modifier.bouncyPress(bouncyState)
            )
        }
    }
}
