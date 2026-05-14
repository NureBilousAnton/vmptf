package com.example.lb3.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lb3.data.Post
import com.example.lb3.ui.BlogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(
    viewModel: BlogViewModel,
    onPostClick: (Int) -> Unit,
    onAddPost: () -> Unit,
    onLogout: () -> Unit
) {
    val posts by viewModel.posts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Блог") },
                actions = {
                    currentUser?.let { user ->
                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                    TextButton(onClick = onLogout) {
                        Text("Вийти")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPost) {
                Icon(Icons.Default.Add, contentDescription = "Додати пост")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearch(it) },
                label = { Text("Пошук статей...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { viewModel.setCategory(null) },
                        label = { Text("Всі") }
                    )
                }
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = {
                            viewModel.setCategory(
                                if (selectedCategoryId == category.id) null else category.id
                            )
                        },
                        label = { Text(category.name) }
                    )
                }
            }

            if (posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Статей не знайдено", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(posts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            categoryName = categories.find { it.id == post.categoryId }?.name ?: "",
                            authorName = viewModel.getUserById(post.authorId)?.username ?: "Невідомий",
                            isOwner = currentUser?.id == post.authorId,
                            onClick = { onPostClick(post.id) },
                            onDelete = { viewModel.deletePost(post.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    categoryName: String,
    authorName: String,
    isOwner: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (isOwner) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Видалити",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            val preview = if (post.content.length > 120) post.content.take(120) + "…" else post.content
            Text(
                text = preview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (categoryName.isNotEmpty()) {
                    AssistChip(onClick = {}, label = { Text(categoryName) })
                }
                Text(
                    text = "автор: $authorName",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
