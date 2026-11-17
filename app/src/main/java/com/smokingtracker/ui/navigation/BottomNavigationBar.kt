package com.smokingtracker.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smokingtracker.ui.Screen
import com.smokingtracker.ui.theme.HapticFeedbackHelper

fun shouldShowBottomBar(currentRoute: String?): Boolean {
    return currentRoute != Screen.Registration.route &&
            currentRoute != Screen.About.route &&
            currentRoute != Screen.Achievements.route &&
            currentRoute != Screen.Statistics.route &&
            currentRoute != Screen.AppearanceSettings.route
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    vibrationEnabled: Boolean = true
) {
    val items = listOf(Screen.Home, Screen.Graph, Screen.Personal)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier
            .animateContentSize()
            .height(68.dp),
        shape = RoundedCornerShape(100),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 3.dp),
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
            toolbarContainerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        items.forEach { screen ->
            val selected = currentRoute?.substringBefore("?") == screen.route
            ShortNavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute?.substringBefore("?") != screen.route) {
                        HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    val extraHeight by animateDpAsState(
                        targetValue = if (selected) 12.dp else 0.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "indicatorHeight"
                    )
                    Box(
                        modifier = Modifier.height(24.dp + extraHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = stringResource(screen.titleResId),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = {
                    AnimatedVisibility(
                        visible = selected,
                        enter = fadeIn() + slideInHorizontally(
                            initialOffsetX = { -15 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) + expandHorizontally(
                            expandFrom = Alignment.Start,
                            clip = false
                        ),
                        exit = fadeOut() + slideOutHorizontally(
                            targetOffsetX = { -15 }
                        ) + shrinkHorizontally(
                            shrinkTowards = Alignment.Start,
                            clip = false
                        )
                    ) {
                        Text(
                            text = stringResource(screen.titleResId),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            modifier = Modifier.padding(start = 2.dp, end = 4.dp)
                        )
                    }
                },
                iconPosition = NavigationItemIconPosition.Start,
                colors = ShortNavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColorStartIconPosition = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColorTopIconPosition = MaterialTheme.colorScheme.onPrimary,
                    selectedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedTextColor = Color.Transparent
                ),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .fillMaxHeight()
            )
        }
    }
}

@Preview
@Composable
fun BottomNavigationBarPreview() {
    BottomNavigationBar(navController = rememberNavController())
}
