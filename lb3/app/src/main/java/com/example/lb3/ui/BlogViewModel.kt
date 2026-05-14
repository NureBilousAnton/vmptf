package com.example.lb3.ui

import androidx.lifecycle.ViewModel
import com.example.lb3.data.BlogRepository
import com.example.lb3.data.Category
import com.example.lb3.data.Comment
import com.example.lb3.data.Post
import com.example.lb3.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BlogViewModel : ViewModel() {
    private val repo = BlogRepository

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refreshCategories()
        refreshPosts()
    }

    fun login(username: String, password: String): Boolean {
        val user = repo.login(username, password)
        return if (user != null) {
            _currentUser.value = user
            true
        } else {
            _error.value = "Невірний логін або пароль"
            false
        }
    }

    fun register(username: String, password: String): Boolean {
        if (username.isBlank() || password.isBlank()) {
            _error.value = "Логін та пароль не можуть бути порожніми"
            return false
        }
        val user = repo.register(username, password)
        return if (user != null) {
            _currentUser.value = user
            true
        } else {
            _error.value = "Такий логін вже існує"
            false
        }
    }

    fun logout() {
        _currentUser.value = null
        _searchQuery.value = ""
        _selectedCategoryId.value = null
    }

    fun setSearch(query: String) {
        _searchQuery.value = query
        refreshPosts()
    }

    fun setCategory(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
        refreshPosts()
    }

    fun refreshPosts() {
        _posts.value = repo.getPosts(_searchQuery.value, _selectedCategoryId.value)
    }

    fun refreshCategories() {
        _categories.value = repo.getCategories()
    }

    fun getPostById(id: Int): Post? = repo.getPostById(id)

    fun addPost(title: String, content: String, categoryId: Int) {
        val user = _currentUser.value ?: return
        repo.addPost(title, content, categoryId, user.id)
        refreshPosts()
    }

    fun updatePost(id: Int, title: String, content: String, categoryId: Int) {
        repo.updatePost(id, title, content, categoryId)
        refreshPosts()
    }

    fun deletePost(id: Int) {
        repo.deletePost(id)
        refreshPosts()
    }

    fun loadComments(postId: Int) {
        _comments.value = repo.getComments(postId)
    }

    fun addComment(postId: Int, content: String) {
        val user = _currentUser.value ?: return
        repo.addComment(postId, user.id, content)
        loadComments(postId)
    }

    fun deleteComment(commentId: Int, postId: Int) {
        repo.deleteComment(commentId)
        loadComments(postId)
    }

    fun getUserById(id: Int): User? = repo.getUserById(id)

    fun clearError() {
        _error.value = null
    }
}
