package com.smokingtracker.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smokingtracker.AchievementsViewModel
import com.smokingtracker.HomeViewModel
import com.smokingtracker.MainViewModel
import com.smokingtracker.StatisticsViewModel
import com.smokingtracker.UpdateCheckState
import com.smokingtracker.R
import com.smokingtracker.ui.components.HomeFabButton
import com.smokingtracker.ui.components.HomeFabMenuOverlay
import com.smokingtracker.ui.components.UpdateDialog

sealed class Screen(val route: String, val titleResId: Int, val icon: ImageVector) {
    data object Registration : Screen("registration", R.string.registration_title, Icons.Filled.Home)
    data object Home : Screen("home", R.string.nav_home, Icons.Filled.Home)
    data object Graph : Screen("graph", R.string.analytics_title, Icons.Filled.BarChart)
    data object Personal : Screen("personal", R.string.nav_personal, Icons.Filled.Settings)
    data object About : Screen("about", R.string.about_app, Icons.Filled.Info)
    data object Achievements : Screen("achievements", R.string.settings_achievements, Icons.Filled.EmojiEvents)
    data object Statistics : Screen("statistics", R.string.settings_statistics, Icons.Filled.BarChart)
    data object AppearanceSettings : Screen("appearance_settings", R.string.settings_appearance, Icons.Filled.Brightness4)
}

@Composable
fun MainApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = org.koin.androidx.compose.koinViewModel()
    val statisticsViewModel: StatisticsViewModel = org.koin.androidx.compose.koinViewModel()
    val achievementsViewModel: AchievementsViewModel = org.koin.androidx.compose.koinViewModel()
    val isRegistered by viewModel.isRegistered.collectAsStateWithLifecycle()

    val checkUpdatesOnStart by viewModel.checkUpdatesOnStart.collectAsStateWithLifecycle()
    val updateCheckState by viewModel.updateCheckState.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()

    var showUpdateDialog by remember { mutableStateOf(false) }
    var latestRelease by remember { mutableStateOf<com.smokingtracker.data.manager.GitHubRelease?>(null) }

    LaunchedEffect(checkUpdatesOnStart) {
        if (checkUpdatesOnStart) {
            viewModel.checkForUpdates(isManual = false)
        }
    }

    LaunchedEffect(updateCheckState) {
        if (updateCheckState is UpdateCheckState.NewUpdate) {
            latestRelease = (updateCheckState as UpdateCheckState.NewUpdate).release
            showUpdateDialog = true
        }
    }

    if (showUpdateDialog) {
        latestRelease?.let { release ->
            UpdateDialog(
                release = release,
                onDismissRequest = {
                    showUpdateDialog = false
                    viewModel.resetUpdateCheckState()
                }
            )
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != Screen.Registration.route && 
                       currentRoute != Screen.About.route &&
                       currentRoute != Screen.Achievements.route &&
                       currentRoute != Screen.Statistics.route &&
                       currentRoute != Screen.AppearanceSettings.route

    val isHomeScreen = currentRoute?.substringBefore("?") == Screen.Home.route
    var isFabMenuExpanded by remember { mutableStateOf(false) }

    var rootSize by remember { mutableStateOf(IntSize.Zero) }
    var fabRightPx by remember { mutableFloatStateOf(0f) }
    var fabTopPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isHomeScreen) {
        if (!isHomeScreen) {
            isFabMenuExpanded = false
        }
    }

    if (isRegistered == null) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    val startDest = if (isRegistered == true) Screen.Home.route else Screen.Registration.route

    LaunchedEffect(isRegistered, currentRoute) {
        if (isRegistered == false && currentRoute != null && currentRoute != Screen.Registration.route) {
            navController.navigate(Screen.Registration.route) {
                popUpTo(0) { inclusive = true }
            }
        } else if (isRegistered == true && currentRoute == Screen.Registration.route) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Registration.route) { inclusive = true }
            }
        }
    }

    val pendingAchievementPopup by achievementsViewModel.pendingAchievementPopup.collectAsStateWithLifecycle()

    if (currentRoute != Screen.Registration.route) {
        pendingAchievementPopup?.let { achievement ->
            AchievementUnlockDialog(
                achievement = achievement,
                vibrationEnabled = vibrationEnabled,
                onDismiss = { achievementsViewModel.dismissAchievementPopup() },
                onNavigateToAchievements = {
                    navController.navigate(Screen.Achievements.route)
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { rootSize = it.size }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            val subScreens = setOf(
                Screen.About.route,
                Screen.Achievements.route,
                Screen.Statistics.route,
                Screen.AppearanceSettings.route
            )
            val mainTabs = listOf(Screen.Home.route, Screen.Graph.route, Screen.Personal.route)

            fun getTabIndex(route: String?): Int {
                val cleanRoute = route?.substringBefore("?")
                return mainTabs.indexOf(cleanRoute)
            }

            val springSpec = spring<IntOffset>(dampingRatio = 0.85f, stiffness = 400f)

            NavHost(
                navController = navController,
                startDestination = startDest,
                modifier = Modifier.padding(innerPadding),
                enterTransition = {
                    val fromRoute = initialState.destination.route
                    val toRoute = targetState.destination.route
                    val fromIndex = getTabIndex(fromRoute)
                    val toIndex = getTabIndex(toRoute)

                    if (toRoute in subScreens) {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = springSpec
                        ) + fadeIn(animationSpec = tween(250))
                    } else if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                        val direction = if (toIndex > fromIndex) {
                            AnimatedContentTransitionScope.SlideDirection.Start
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.End
                        }
                        slideIntoContainer(
                            towards = direction,
                            animationSpec = springSpec
                        ) + fadeIn(animationSpec = tween(250))
                    } else {
                        fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.96f, animationSpec = tween(250))
                    }
                },
                exitTransition = {
                    val fromRoute = initialState.destination.route
                    val toRoute = targetState.destination.route
                    val fromIndex = getTabIndex(fromRoute)
                    val toIndex = getTabIndex(toRoute)

                    if (toRoute in subScreens) {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = springSpec
                        ) + fadeOut(animationSpec = tween(200))
                    } else if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                        val direction = if (toIndex > fromIndex) {
                            AnimatedContentTransitionScope.SlideDirection.Start
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.End
                        }
                        slideOutOfContainer(
                            towards = direction,
                            animationSpec = springSpec
                        ) + fadeOut(animationSpec = tween(200))
                    } else {
                        fadeOut(animationSpec = tween(200))
                    }
                },
                popEnterTransition = {
                    val fromRoute = initialState.destination.route
                    val toRoute = targetState.destination.route
                    val fromIndex = getTabIndex(fromRoute)
                    val toIndex = getTabIndex(toRoute)

                    if (fromRoute in subScreens) {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = springSpec
                        ) + fadeIn(animationSpec = tween(250))
                    } else if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                        val direction = if (toIndex > fromIndex) {
                            AnimatedContentTransitionScope.SlideDirection.Start
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.End
                        }
                        slideIntoContainer(
                            towards = direction,
                            animationSpec = springSpec
                        ) + fadeIn(animationSpec = tween(250))
                    } else {
                        fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.96f, animationSpec = tween(250))
                    }
                },
                popExitTransition = {
                    val fromRoute = initialState.destination.route
                    val toRoute = targetState.destination.route
                    val fromIndex = getTabIndex(fromRoute)
                    val toIndex = getTabIndex(toRoute)

                    if (fromRoute in subScreens) {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = springSpec
                        ) + fadeOut(animationSpec = tween(200))
                    } else if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                        val direction = if (toIndex > fromIndex) {
                            AnimatedContentTransitionScope.SlideDirection.Start
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.End
                        }
                        slideOutOfContainer(
                            towards = direction,
                            animationSpec = springSpec
                        ) + fadeOut(animationSpec = tween(200))
                    } else {
                        fadeOut(animationSpec = tween(200))
                    }
                }
            ) {
                composable(Screen.Registration.route) {
                    RegistrationScreen(
                        onRegister = { viewModel.registerUser() }
                    )
                }
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        vibrationEnabled = vibrationEnabled,
                        onNavigateToAchievements = { navController.navigate(Screen.Achievements.route) },
                        onNavigateToGraphs = { target ->
                            statisticsViewModel.setGraphScrollTarget(target)
                            navController.navigate(Screen.Graph.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(Screen.Graph.route) {
                    GraphScreen(
                        viewModel = statisticsViewModel,
                        onNavigateToSettings = {
                            navController.navigate(Screen.Personal.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(Screen.Personal.route) {
                    PersonalScreen(
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
                    AchievementsScreen(viewModel = achievementsViewModel, onBack = { navController.popBackStack() })
                }
                composable(Screen.Statistics.route) {
                    StatisticsScreen(
                        viewModel = statisticsViewModel,
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
                    AppearanceSettingsScreen(onBack = { navController.popBackStack() })
                }
            }
        }

        HomeFabMenuOverlay(
            isExpanded = isFabMenuExpanded && isHomeScreen,
            onDismiss = { isFabMenuExpanded = false },
            rootSize = rootSize,
            fabRightPx = fabRightPx,
            fabTopPx = fabTopPx,
            vibrationEnabled = vibrationEnabled,
            onMindfulPause = { homeViewModel.triggerFabMindfulPause() },
            onResisted = { homeViewModel.triggerFabResisted() },
            onSmoked = { homeViewModel.triggerFabSmoked() }
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.BottomCenter
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.wrapContentWidth()
                ) {
                    BottomNavigationBar(
                        navController = navController,
                        vibrationEnabled = vibrationEnabled
                    )

                    AnimatedVisibility(
                        visible = isHomeScreen,
                        enter = scaleIn(spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)) +
                                expandHorizontally(
                                    animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
                                    clip = false
                                ) +
                                fadeIn(tween(150)),
                        exit = scaleOut(spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)) +
                               shrinkHorizontally(
                                    animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
                                    clip = false
                               ) +
                               fadeOut(tween(150))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(10.dp))
                            HomeFabButton(
                                isExpanded = isFabMenuExpanded,
                                onToggle = { isFabMenuExpanded = !isFabMenuExpanded },
                                vibrationEnabled = vibrationEnabled,
                                modifier = Modifier.onGloballyPositioned { coordinates ->
                                    val pos = coordinates.positionInRoot()
                                    fabRightPx = pos.x + coordinates.size.width
                                    fabTopPx = pos.y
                                }
                            )
                        }
                    }
                }
            }
        }
    }
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
