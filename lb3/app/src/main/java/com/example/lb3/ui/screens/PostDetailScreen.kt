package com.example.lb3.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lb3.data.Comment
import com.example.lb3.ui.BlogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    viewModel: BlogViewModel,
    postId: Int,
    onEditPost: (Int) -> Unit,
    onBack: () -> Unit
) {
    val post = remember(postId) { viewModel.getPostById(postId) }
    val comments by viewModel.comments.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(postId) {
        viewModel.loadComments(postId)
    }

    if (post == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val category = categories.find { it.id == post.categoryId }
    val author = viewModel.getUserById(post.authorId)
    val isOwner = currentUser?.id == post.authorId

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = post.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (isOwner) {
                        IconButton(onClick = { onEditPost(postId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Редагувати")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    category?.let { AssistChip(onClick = {}, label = { Text(it.name) }) }
                    Text(
                        text = "автор: ${author?.username ?: "Невідомий"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "Коментарі (${comments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(comments, key = { it.id }) { comment ->
                CommentItem(
                    comment = comment,
                    authorName = viewModel.getUserById(comment.authorId)?.username ?: "Невідомий",
                    isOwner = currentUser?.id == comment.authorId,
                    onDelete = { viewModel.deleteComment(comment.id, postId) }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text("Додати коментар...") },
                        modifier = Modifier.weight(1f),
                        minLines = 1,
                        maxLines = 3
                    )
                    Spacer(Modifier.width(4.dp))
                    Button(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                viewModel.addComment(postId, commentText.trim())
                                commentText = ""
                            }
                        }
                    ) {
                        Text("Надіслати")
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    authorName: String,
    isOwner: Boolean,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = authorName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (isOwner) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Видалити коментар",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
