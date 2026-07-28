package com.otp.authenticator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.otp.authenticator.ui.screens.AddAccountScreen
import com.otp.authenticator.ui.screens.HomeScreen
import com.otp.authenticator.ui.screens.ScanScreen
import com.otp.authenticator.ui.theme.OtpAuthenticatorTheme
import com.otp.authenticator.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OtpAuthenticatorTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToScan = { navController.navigate("scan") },
                onNavigateToAdd = { navController.navigate("add") }
            )
        }
        composable("scan") {
            ScanScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onAccountParsed = { navController.popBackStack() }
            )
        }
        composable("add") {
            AddAccountScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
