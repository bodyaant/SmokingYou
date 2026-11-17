package com.smokingtracker.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smokingtracker.AchievementsViewModel
import com.smokingtracker.HomeViewModel
import com.smokingtracker.MainViewModel
import com.smokingtracker.R
import com.smokingtracker.StatisticsViewModel
import com.smokingtracker.ui.components.HomeFabButton
import com.smokingtracker.ui.navigation.BottomNavigationBar
import com.smokingtracker.ui.navigation.GlobalOverlayHost
import com.smokingtracker.ui.navigation.rememberFabAnchorCoordinates
import com.smokingtracker.ui.navigation.shouldShowBottomBar
import com.smokingtracker.ui.navigation.trackFabPosition
import com.smokingtracker.ui.navigation.trackRootSize
import org.koin.androidx.compose.koinViewModel

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
    val homeViewModel: HomeViewModel = koinViewModel()
    val statisticsViewModel: StatisticsViewModel = koinViewModel()
    val achievementsViewModel: AchievementsViewModel = koinViewModel()

    val isRegistered by viewModel.isRegistered.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isHomeScreen = currentRoute?.substringBefore("?") == Screen.Home.route

    var isFabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val fabAnchor = rememberFabAnchorCoordinates()

    if (isRegistered == null) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    val startDest = remember { if (isRegistered == true) Screen.Home.route else Screen.Registration.route }

    LaunchedEffect(isRegistered, currentRoute) {
        if (isRegistered == false && currentRoute != null && currentRoute != Screen.Registration.route) {
            navController.navigate(Screen.Registration.route) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        } else if (isRegistered == true && currentRoute == Screen.Registration.route) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Registration.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .trackRootSize(fabAnchor)
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                startDestination = startDest,
                viewModel = viewModel,
                homeViewModel = homeViewModel,
                statisticsViewModel = statisticsViewModel,
                achievementsViewModel = achievementsViewModel,
                vibrationEnabled = vibrationEnabled,
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            )
        }

        GlobalOverlayHost(
            mainViewModel = viewModel,
            achievementsViewModel = achievementsViewModel,
            homeViewModel = homeViewModel,
            navController = navController,
            currentRoute = currentRoute,
            vibrationEnabled = vibrationEnabled,
            isHomeScreen = isHomeScreen,
            isFabMenuExpanded = isFabMenuExpanded,
            onDismissFabMenu = { isFabMenuExpanded = false },
            fabAnchor = fabAnchor
        )

        val showBottomBar = shouldShowBottomBar(currentRoute)

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
                                modifier = Modifier.trackFabPosition(fabAnchor)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    viewModel: MainViewModel,
    homeViewModel: HomeViewModel,
    statisticsViewModel: StatisticsViewModel,
    achievementsViewModel: AchievementsViewModel,
    vibrationEnabled: Boolean,
    modifier: Modifier = Modifier
) {
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
        startDestination = startDestination,
        modifier = modifier,
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
                onNavigateToAchievements = {
                    navController.navigate(Screen.Achievements.route) {
                        launchSingleTop = true
                    }
                },
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
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToAchievements = {
                    navController.navigate(Screen.Achievements.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToStatistics = {
                    navController.navigate(Screen.Statistics.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToAppearance = {
                    navController.navigate(Screen.AppearanceSettings.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.About.route) {
            AboutScreen(onBack = { navController.navigateUp() })
        }
        composable(Screen.Achievements.route) {
            AchievementsScreen(viewModel = achievementsViewModel, onBack = { navController.navigateUp() })
        }
        composable(Screen.Statistics.route) {
            StatisticsScreen(
                viewModel = statisticsViewModel,
                onBack = { navController.navigateUp() },
                onNavigateToSettings = {
                    navController.navigate(Screen.Personal.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(Screen.AppearanceSettings.route) {
            AppearanceSettingsScreen(onBack = { navController.navigateUp() })
        }
    }
}
