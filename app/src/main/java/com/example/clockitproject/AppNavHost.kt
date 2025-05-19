package com.example.clockitproject

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.clockitproject.auth.AuthScreen
import com.google.firebase.auth.FirebaseAuth

private object Routes {
    const val Auth     = "auth"
    const val Calendar = "calendar"
    const val Active   = "active"
    const val Profile  = "profile"
}

private data class NavItem(val route: String, val icon: ImageVector, val contentDescription: String)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val taskViewModel: TaskViewModel = viewModel()

    val startDestination = if (FirebaseAuth.getInstance().currentUser == null) {
        Routes.Auth
    } else {
        Routes.Calendar
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.Auth) {
            AuthScreen(onLoginSuccess = {
                navController.navigate(Routes.Calendar) {
                    popUpTo(Routes.Auth) { inclusive = true }
                }
            })
        }

        composable(Routes.Calendar) {
            MainScaffold(navController, Routes.Calendar) {
                ScheduleScreen(taskViewModel)
            }
        }

        composable(Routes.Active) {
            MainScaffold(navController, Routes.Active) {
                ActiveScreen(taskViewModel)
            }
        }

        composable(Routes.Profile) {
            MainScaffold(navController, Routes.Profile) {
                ProfileScreen(navController)
            }
        }
    }
}

@Composable
fun MainScaffold(
    navController: NavHostController,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    val items = listOf(
        NavItem(Routes.Calendar, Icons.Filled.CalendarToday, "Calendar"),
        NavItem(Routes.Active, Icons.Filled.Visibility, "Active"),
        NavItem(Routes.Profile, Icons.Filled.Person, "Profile")
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.contentDescription) },
                        selected = (item.route == currentRoute),
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(Routes.Calendar) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}
