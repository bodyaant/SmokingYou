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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smokingtracker.HomeViewModel
import com.smokingtracker.MainViewModel
import com.smokingtracker.UpdateCheckState
import com.smokingtracker.R

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
    val isRegistered by viewModel.isRegistered.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
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

    val isHomeScreen = currentRoute?.substringBefore("?") == Screen.Home.route
    var isFabMenuExpanded by remember { mutableStateOf(false) }

    var rootSize by remember { mutableStateOf(IntSize.Zero) }
    var fabRightPx by remember { mutableFloatStateOf(0f) }
    var fabTopPx by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

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

    val pendingAchievementPopup by viewModel.pendingAchievementPopup.collectAsStateWithLifecycle()

    if (currentRoute != Screen.Registration.route) {
        pendingAchievementPopup?.let { achievement ->
            AchievementUnlockDialog(
                achievement = achievement,
                vibrationEnabled = vibrationEnabled,
                onDismiss = { viewModel.dismissAchievementPopup() },
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
                    RegistrationScreen(viewModel, navController)
                }
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        vibrationEnabled = vibrationEnabled,
                        onNavigateToAchievements = { navController.navigate(Screen.Achievements.route) },
                        onNavigateToGraphs = { target ->
                            viewModel.setGraphScrollTarget(target)
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
                        viewModel = viewModel,
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

        if (isFabMenuExpanded && isHomeScreen) {
            androidx.activity.compose.BackHandler {
                isFabMenuExpanded = false
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isFabMenuExpanded = false
                    }
            )
        }

        if (rootSize.width > 0 && fabRightPx > 0f) {
            AnimatedVisibility(
                visible = isFabMenuExpanded && isHomeScreen,
                enter = fadeIn(tween(150)) + scaleIn(
                    initialScale = 0.8f,
                    transformOrigin = TransformOrigin(1f, 1f),
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow)
                ) + slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow)
                ),
                exit = fadeOut(tween(100)) + scaleOut(
                    targetScale = 0.8f,
                    transformOrigin = TransformOrigin(1f, 1f),
                    animationSpec = tween(100)
                ) + slideOutVertically(
                    targetOffsetY = { it / 3 },
                    animationSpec = tween(100)
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = with(density) { (rootSize.width - fabRightPx).coerceAtLeast(0f).toDp() },
                        bottom = with(density) { (rootSize.height - fabTopPx + 12.dp.toPx()).toDp() }
                    )
            ) {
                FabMenuItemsColumn(
                    onMindfulPause = {
                        isFabMenuExpanded = false
                        com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                        homeViewModel.triggerFabMindfulPause()
                    },
                    onResisted = {
                        isFabMenuExpanded = false
                        homeViewModel.triggerFabResisted()
                    },
                    onSmoked = {
                        isFabMenuExpanded = false
                        com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
                        homeViewModel.triggerFabSmoked()
                    }
                )
            }
        }

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
                                expandHorizontally(spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow)) +
                                fadeIn(tween(150)),
                        exit = scaleOut(spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)) +
                               shrinkHorizontally(spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)) +
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
    vibrationEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val items = listOf(Screen.Home, Screen.Graph, Screen.Personal)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier
            .animateContentSize()
            .height(68.dp),
        shape = RoundedCornerShape(100),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeFabButton(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    vibrationEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val squishProgress = remember { Animatable(1f) }

    val triggerSquish: () -> Unit = {
        scope.launch {
            squishProgress.animateTo(
                targetValue = 0.82f,
                animationSpec = tween(70, easing = LinearEasing)
            )
            squishProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow)
            )
        }
    }

    ToggleFloatingActionButton(
        checked = isExpanded,
        onCheckedChange = {
            onToggle()
            triggerSquish()
            com.smokingtracker.ui.theme.HapticFeedbackHelper.performClick(vibrationEnabled, haptic, context)
        },
        modifier = modifier
            .size(68.dp)
            .graphicsLayer {
                scaleX = 2f - squishProgress.value
                scaleY = squishProgress.value
            },
        containerColor = ToggleFloatingActionButtonDefaults.containerColor(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        ),
        containerCornerRadius = ToggleFloatingActionButtonDefaults.containerCornerRadius(
            24.dp,
            36.dp
        ),
        containerSize = ToggleFloatingActionButtonDefaults.containerSize(
            68.dp,
            64.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isExpanded,
                transitionSpec = {
                    (scaleIn(spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium)) + fadeIn())
                        .togetherWith(scaleOut(tween(100)) + fadeOut())
                },
                label = "fab_icon_morph"
            ) { expanded ->
                if (expanded) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.close_menu),
                        tint = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_entry),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FabMenuItemsColumn(
    onMindfulPause: () -> Unit,
    onResisted: () -> Unit,
    onSmoked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FabMenuItem(
            onClick = onMindfulPause,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.SelfImprovement,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            },
            label = stringResource(R.string.action_mindful_pause)
        )

        FabMenuItem(
            onClick = onResisted,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            },
            label = stringResource(R.string.action_resisted_craving)
        )

        FabMenuItem(
            onClick = onSmoked,
            icon = {
                Icon(
                    imageVector = Icons.Filled.SmokingRooms,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            },
            label = stringResource(R.string.action_smoked_cigarette)
        )
    }
}

@Composable
fun FabMenuItem(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shadowElevation = 6.dp,
        tonalElevation = 3.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            icon()
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Preview
@Composable
fun BottomNavigationBarPreview() {
    BottomNavigationBar(navController = rememberNavController())
}
