package com.example.lb3.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lb3.ui.screens.LoginScreen
import com.example.lb3.ui.screens.PostDetailScreen
import com.example.lb3.ui.screens.PostFormScreen
import com.example.lb3.ui.screens.PostListScreen

@Composable
fun BlogNavigation(viewModel: BlogViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("posts") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("posts") {
            PostListScreen(
                viewModel = viewModel,
                onPostClick = { postId -> navController.navigate("post/$postId") },
                onAddPost = { navController.navigate("post_form") },
                onLogout = {
                    viewModel.logout()
                    navController.navigate("login") {
                        popUpTo("posts") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "post/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.IntType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getInt("postId") ?: return@composable
            PostDetailScreen(
                viewModel = viewModel,
                postId = postId,
                onEditPost = { id -> navController.navigate("post_form?postId=$id") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "post_form?postId={postId}",
            arguments = listOf(
                navArgument("postId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val rawId = backStackEntry.arguments?.getInt("postId") ?: -1
            val postId = if (rawId == -1) null else rawId
            PostFormScreen(
                viewModel = viewModel,
                postId = postId,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
