package com.smokingtracker.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.smokingtracker.AchievementsViewModel
import com.smokingtracker.HomeViewModel
import com.smokingtracker.MainViewModel
import com.smokingtracker.UpdateCheckState
import com.smokingtracker.ui.AchievementUnlockDialog
import com.smokingtracker.ui.Screen
import com.smokingtracker.ui.components.HomeFabMenuOverlay
import com.smokingtracker.ui.components.UpdateDialog
import kotlin.math.abs

@Stable
class FabAnchorCoordinates {
    var rootSize by mutableStateOf(IntSize.Zero)
    var fabRightPx by mutableFloatStateOf(0f)
    var fabTopPx by mutableFloatStateOf(0f)

    fun updateRootSize(size: IntSize) {
        if (rootSize != size) {
            rootSize = size
        }
    }

    fun updateFabPosition(newRight: Float, newTop: Float) {
        if (abs(fabRightPx - newRight) > 0.5f) {
            fabRightPx = newRight
        }
        if (abs(fabTopPx - newTop) > 0.5f) {
            fabTopPx = newTop
        }
    }
}

@Composable
fun rememberFabAnchorCoordinates(): FabAnchorCoordinates = remember { FabAnchorCoordinates() }

fun Modifier.trackFabPosition(fabAnchor: FabAnchorCoordinates): Modifier = this.onGloballyPositioned { coordinates ->
    val pos = coordinates.positionInRoot()
    val newRight = pos.x + coordinates.size.width
    val newTop = pos.y
    fabAnchor.updateFabPosition(newRight, newTop)
}

fun Modifier.trackRootSize(fabAnchor: FabAnchorCoordinates): Modifier = this.onGloballyPositioned { coordinates ->
    fabAnchor.updateRootSize(coordinates.size)
}

@Composable
fun BoxScope.GlobalOverlayHost(
    mainViewModel: MainViewModel,
    achievementsViewModel: AchievementsViewModel,
    homeViewModel: HomeViewModel,
    navController: NavHostController,
    currentRoute: String?,
    vibrationEnabled: Boolean,
    isHomeScreen: Boolean,
    isFabMenuExpanded: Boolean,
    onDismissFabMenu: () -> Unit,
    fabAnchor: FabAnchorCoordinates
) {
    UpdateOverlay(viewModel = mainViewModel)

    AchievementOverlay(
        achievementsViewModel = achievementsViewModel,
        navController = navController,
        currentRoute = currentRoute,
        vibrationEnabled = vibrationEnabled
    )

    FabMenuOverlay(
        homeViewModel = homeViewModel,
        isHomeScreen = isHomeScreen,
        isFabMenuExpanded = isFabMenuExpanded,
        onDismissFabMenu = onDismissFabMenu,
        fabAnchor = fabAnchor,
        vibrationEnabled = vibrationEnabled
    )
}

@Composable
fun GlobalOverlayHost(
    mainViewModel: MainViewModel,
    achievementsViewModel: AchievementsViewModel,
    homeViewModel: HomeViewModel,
    navController: NavHostController,
    currentRoute: String?,
    vibrationEnabled: Boolean,
    isHomeScreen: Boolean,
    isFabMenuExpanded: Boolean,
    onDismissFabMenu: () -> Unit,
    fabAnchor: FabAnchorCoordinates,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        GlobalOverlayHost(
            mainViewModel = mainViewModel,
            achievementsViewModel = achievementsViewModel,
            homeViewModel = homeViewModel,
            navController = navController,
            currentRoute = currentRoute,
            vibrationEnabled = vibrationEnabled,
            isHomeScreen = isHomeScreen,
            isFabMenuExpanded = isFabMenuExpanded,
            onDismissFabMenu = onDismissFabMenu,
            fabAnchor = fabAnchor
        )
    }
}

@Composable
private fun UpdateOverlay(
    viewModel: MainViewModel
) {
    val checkUpdatesOnStart by viewModel.checkUpdatesOnStart.collectAsStateWithLifecycle()
    val updateCheckState by viewModel.updateCheckState.collectAsStateWithLifecycle()
    var updateDialogDismissed by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(checkUpdatesOnStart) {
        if (checkUpdatesOnStart) {
            viewModel.checkForUpdates(isManual = false)
        }
    }

    LaunchedEffect(updateCheckState) {
        if (updateCheckState is UpdateCheckState.NewUpdate) {
            updateDialogDismissed = false
        }
    }

    val activeRelease = (updateCheckState as? UpdateCheckState.NewUpdate)?.release
    if (!updateDialogDismissed && activeRelease != null) {
        UpdateDialog(
            release = activeRelease,
            onDismissRequest = {
                updateDialogDismissed = true
                viewModel.resetUpdateCheckState()
            }
        )
    }
}

@Composable
private fun AchievementOverlay(
    achievementsViewModel: AchievementsViewModel,
    navController: NavHostController,
    currentRoute: String?,
    vibrationEnabled: Boolean
) {
    val pendingAchievementPopup by achievementsViewModel.pendingAchievementPopup.collectAsStateWithLifecycle()

    if (currentRoute != Screen.Registration.route && currentRoute != Screen.Achievements.route) {
        pendingAchievementPopup?.let { achievement ->
            AchievementUnlockDialog(
                achievement = achievement,
                vibrationEnabled = vibrationEnabled,
                onDismiss = { achievementsViewModel.dismissAchievementPopup() },
                onNavigateToAchievements = {
                    achievementsViewModel.clearAchievementPopupQueue()
                    navController.navigate(Screen.Achievements.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
private fun BoxScope.FabMenuOverlay(
    homeViewModel: HomeViewModel,
    isHomeScreen: Boolean,
    isFabMenuExpanded: Boolean,
    onDismissFabMenu: () -> Unit,
    fabAnchor: FabAnchorCoordinates,
    vibrationEnabled: Boolean
) {
    LaunchedEffect(isHomeScreen) {
        if (!isHomeScreen) {
            onDismissFabMenu()
        }
    }

    HomeFabMenuOverlay(
        isExpanded = isFabMenuExpanded && isHomeScreen,
        onDismiss = onDismissFabMenu,
        rootSize = fabAnchor.rootSize,
        fabRightPx = fabAnchor.fabRightPx,
        fabTopPx = fabAnchor.fabTopPx,
        vibrationEnabled = vibrationEnabled,
        onMindfulPause = { homeViewModel.triggerFabMindfulPause() },
        onResisted = { homeViewModel.triggerFabResisted() },
        onSmoked = { homeViewModel.triggerFabSmoked() }
    )
}
