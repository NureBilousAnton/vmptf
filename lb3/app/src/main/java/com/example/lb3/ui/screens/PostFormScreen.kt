package com.example.lb3.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lb3.ui.BlogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostFormScreen(
    viewModel: BlogViewModel,
    postId: Int?,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val post = remember(postId) { postId?.let { viewModel.getPostById(it) } }
    val categories by viewModel.categories.collectAsState()

    var title by remember { mutableStateOf(post?.title ?: "") }
    var content by remember { mutableStateOf(post?.content ?: "") }
    var selectedCategoryId by remember {
        mutableStateOf(post?.categoryId ?: categories.firstOrNull()?.id ?: 1)
    }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }
    var contentError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (postId == null) "Нова стаття" else "Редагування статті") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Скасувати")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            titleError = title.isBlank()
                            contentError = content.isBlank()
                            if (!titleError && !contentError) {
                                if (postId == null) {
                                    viewModel.addPost(title.trim(), content.trim(), selectedCategoryId)
                                } else {
                                    viewModel.updatePost(postId, title.trim(), content.trim(), selectedCategoryId)
                                }
                                onSave()
                            }
                        }
                    ) {
                        Text("Зберегти")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                label = { Text("Заголовок") },
                singleLine = true,
                isError = titleError,
                supportingText = { if (titleError) Text("Введіть заголовок") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = categories.find { it.id == selectedCategoryId }?.name ?: "Оберіть категорію",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Категорія") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategoryId = category.id
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = content,
                onValueChange = { content = it; contentError = false },
                label = { Text("Текст статті") },
                isError = contentError,
                supportingText = { if (contentError) Text("Введіть текст статті") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                minLines = 6
            )
        }
    }
}
