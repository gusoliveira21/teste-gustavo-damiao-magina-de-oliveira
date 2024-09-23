package com.aiko.aikospvision.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aiko.aikospvision.ui.screens_app.home.ScreenHome
import com.aiko.aikospvision.ui.screens_app.stops.ScreenStops
import com.aiko.aikospvision.ui.theme.AikoSPVisionTheme

@Composable
fun NavigationHost() {
    val navController = rememberNavController()
    AikoSPVisionTheme() {
        NavHost(navController = navController, startDestination = "home_screen") {
            composable(route = "home_screen") { ScreenHome(navController) }

            composable(
                route = "stops_screen/{paradaId}",
                arguments = listOf(navArgument("paradaId") { type = NavType.LongType })
            ) { backStackEntry ->
                val paradaId = backStackEntry.arguments?.getLong("paradaId") ?: null
                ScreenStops(navController = navController, paradaId = paradaId)
            }
        }
    }
}