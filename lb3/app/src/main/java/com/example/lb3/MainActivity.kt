package com.example.lb3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.lb3.data.BlogRepository
import com.example.lb3.ui.BlogNavigation
import com.example.lb3.ui.BlogViewModel
import com.example.lb3.ui.theme.LB3Theme

class MainActivity : ComponentActivity() {
    private val viewModel: BlogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BlogRepository.init(applicationContext)
        viewModel.refreshCategories()
        viewModel.refreshPosts()

        enableEdgeToEdge()
        setContent {
            LB3Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BlogNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
