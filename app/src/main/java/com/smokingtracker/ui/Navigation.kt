package com.smokingtracker.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smokingtracker.MainViewModel
import com.smokingtracker.R

sealed class Screen(val route: String, val titleResId: Int, val icon: ImageVector) {
    object Registration : Screen("registration", R.string.registration_title, Icons.Filled.Home)
    object Home : Screen("home", R.string.nav_home, Icons.Filled.Home)
    object Graph : Screen("graph", R.string.nav_graph, Icons.Filled.BarChart)
    object Personal : Screen("personal", R.string.nav_personal, Icons.Filled.Settings)
    object About : Screen("about", R.string.about_app, Icons.Filled.Info)
    object Achievements : Screen("achievements", R.string.settings_achievements, Icons.Filled.EmojiEvents)
    object Statistics : Screen("statistics", R.string.settings_statistics, Icons.Filled.BarChart)
    object AppearanceSettings : Screen("appearance_settings", R.string.settings_appearance, Icons.Filled.Brightness4)
}


@Composable
fun MainApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val isRegistered by viewModel.isRegistered.collectAsState()

    val context = LocalContext.current
    val checkUpdatesOnStart by viewModel.checkUpdatesOnStart.collectAsState()
    val updateCheckState by viewModel.updateCheckState.collectAsState()

    var showUpdateDialog by remember { mutableStateOf(false) }
    var latestRelease by remember { mutableStateOf<com.smokingtracker.data.manager.GitHubRelease?>(null) }

    LaunchedEffect(checkUpdatesOnStart) {
        if (checkUpdatesOnStart) {
            viewModel.checkForUpdates(isManual = false)
        }
    }

    LaunchedEffect(updateCheckState) {
        if (updateCheckState is MainViewModel.UpdateCheckState.NewUpdate) {
            latestRelease = (updateCheckState as MainViewModel.UpdateCheckState.NewUpdate).release
            showUpdateDialog = true
        }
    }

    if (showUpdateDialog && latestRelease != null) {
        AlertDialog(
            onDismissRequest = {
                showUpdateDialog = false
                viewModel.resetUpdateCheckState()
            },
            confirmButton = {
                Button(
                    onClick = {
                        val apkAsset = latestRelease?.assets?.firstOrNull { it.name?.endsWith(".apk") == true }
                        val downloadUrl = apkAsset?.browserDownloadUrl ?: latestRelease?.htmlUrl
                        if (!downloadUrl.isNullOrEmpty()) {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(downloadUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        showUpdateDialog = false
                        viewModel.resetUpdateCheckState()
                    }
                ) {
                    Text(stringResource(R.string.update_dialog_download), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUpdateDialog = false
                        viewModel.resetUpdateCheckState()
                    }
                ) {
                    Text(stringResource(R.string.update_dialog_later), fontWeight = FontWeight.Bold)
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.SystemUpdate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.update_dialog_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.update_dialog_message, latestRelease?.tagName.orEmpty()),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val bodyText = latestRelease?.body
                    if (!bodyText.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp)
                        ) {
                            Box(modifier = Modifier.verticalScroll(rememberScrollState()).padding(12.dp)) {
                                Text(
                                    text = bodyText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != Screen.Registration.route && 
                       currentRoute != Screen.About.route &&
                       currentRoute != Screen.Achievements.route &&
                       currentRoute != Screen.Statistics.route &&
                       currentRoute != Screen.AppearanceSettings.route

    if (isRegistered == null) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    val startDest = if (isRegistered == true) Screen.Home.route else Screen.Registration.route

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDest,
                modifier = Modifier.padding(innerPadding),
                enterTransition = {
                    fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(300))
                }
            ) {
                composable(Screen.Registration.route) {
                    RegistrationScreen(viewModel, navController)
                }
                composable(Screen.Home.route) {
                    HomeScreen(viewModel)
                }
                composable(Screen.Graph.route) {
                    GraphScreen(
                        viewModel = viewModel,
                        onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) }
                    )
                }
                composable(Screen.Personal.route) {
                    PersonalScreen(
                        viewModel = viewModel,
                        onNavigateToAbout = { navController.navigate(Screen.About.route) },
                        onNavigateToAchievements = { navController.navigate(Screen.Achievements.route) },
                        onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) },
                        onNavigateToAppearance = { navController.navigate(Screen.AppearanceSettings.route) }
                    )
                }
                composable(Screen.About.route) {
                    AboutScreen(onBack = { navController.navigateUp() })
                }
                composable(Screen.Achievements.route) {
                    AchievementsScreen(viewModel, onBack = { navController.popBackStack() })
                }
                composable(Screen.Statistics.route) {
                    StatisticsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onNavigateToSettings = {
                            navController.popBackStack()
                            navController.navigate(Screen.Personal.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(Screen.AppearanceSettings.route) {
                    AppearanceSettingsScreen(viewModel, onBack = { navController.popBackStack() })
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(
                    initialOffsetY = { it }, 
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
                ) + fadeIn(tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { it }, 
                    animationSpec = tween(300)
                ) + fadeOut(tween(300))
            ) {
                BottomNavigationBar(navController = navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(Screen.Home, Screen.Graph, Screen.Personal)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    HorizontalFloatingToolbar(
        expanded = true,
        modifier = Modifier
            .animateContentSize()
            .height(68.dp),
        shape = RoundedCornerShape(100),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
            toolbarContainerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        items.forEach { screen ->
            val selected = currentRoute == screen.route
            ShortNavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    val extraHeight by animateDpAsState(
                        targetValue = if (selected) 16.dp else 0.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "indicatorHeight"
                    )
                    Box(
                        modifier = Modifier.height(26.dp + extraHeight),
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
                        ) + expandHorizontally(expandFrom = Alignment.Start),
                        exit = fadeOut() + slideOutHorizontally(
                            targetOffsetX = { -15 }
                        ) + shrinkHorizontally(shrinkTowards = Alignment.Start)
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
                    selectedTextColor = MaterialTheme.colorScheme.onPrimary,
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
