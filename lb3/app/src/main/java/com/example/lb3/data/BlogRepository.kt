package com.example.lb3.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class User(val id: Int, val username: String, val password: String)
data class Category(val id: Int, val name: String)
data class Post(
    val id: Int,
    val title: String,
    val content: String,
    val categoryId: Int,
    val authorId: Int,
    val createdAt: Long = System.currentTimeMillis()
)
data class Comment(
    val id: Int,
    val postId: Int,
    val authorId: Int,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

object BlogRepository {
    private val users = mutableListOf<User>()
    private val categories = mutableListOf<Category>()
    private val posts = mutableListOf<Post>()
    private val comments = mutableListOf<Comment>()

    // Cache entries: key -> (timestamp millis, result list)
    private val postCache = mutableMapOf<String, Pair<Long, List<Post>>>()
    private const val CACHE_TTL_MS = 30_000L

    private var nextUserId = 1
    private var nextCategoryId = 1
    private var nextPostId = 1
    private var nextCommentId = 1

    private lateinit var prefs: SharedPreferences
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        prefs = context.getSharedPreferences("blog_data", Context.MODE_PRIVATE)
        loadData()
        if (categories.isEmpty()) {
            addCategory("Technology")
            addCategory("Science")
            addCategory("Arts")
            addCategory("Sports")
        }
    }

    // --- Users ---

    fun register(username: String, password: String): User? {
        if (users.any { it.username == username }) return null
        val user = User(nextUserId++, username, password)
        users.add(user)
        saveData()
        return user
    }

    fun login(username: String, password: String): User? =
        users.find { it.username == username && it.password == password }

    fun getUserById(id: Int): User? = users.find { it.id == id }

    // --- Categories ---

    fun getCategories(): List<Category> = categories.toList()

    fun getCategoryById(id: Int): Category? = categories.find { it.id == id }

    private fun addCategory(name: String) {
        categories.add(Category(nextCategoryId++, name))
        saveData()
    }

    // --- Posts ---

    fun getPosts(search: String = "", categoryId: Int? = null): List<Post> {
        val cacheKey = "$search|${categoryId ?: ""}"
        val cached = postCache[cacheKey]
        if (cached != null && System.currentTimeMillis() - cached.first < CACHE_TTL_MS) {
            return cached.second
        }

        val result = posts
            .filter { post ->
                val matchesSearch = search.isBlank() ||
                    post.title.contains(search, ignoreCase = true) ||
                    post.content.contains(search, ignoreCase = true)
                val matchesCategory = categoryId == null || post.categoryId == categoryId
                matchesSearch && matchesCategory
            }
            .sortedByDescending { it.createdAt }

        postCache[cacheKey] = System.currentTimeMillis() to result
        return result
    }

    fun getPostById(id: Int): Post? = posts.find { it.id == id }

    fun addPost(title: String, content: String, categoryId: Int, authorId: Int): Post {
        val post = Post(nextPostId++, title, content, categoryId, authorId)
        posts.add(post)
        invalidateCache()
        saveData()
        return post
    }

    fun updatePost(id: Int, title: String, content: String, categoryId: Int): Post? {
        val idx = posts.indexOfFirst { it.id == id }
        if (idx == -1) return null
        val updated = posts[idx].copy(title = title, content = content, categoryId = categoryId)
        posts[idx] = updated
        invalidateCache()
        saveData()
        return updated
    }

    fun deletePost(id: Int) {
        posts.removeIf { it.id == id }
        comments.removeIf { it.postId == id }
        invalidateCache()
        saveData()
    }

    // --- Comments ---

    fun getComments(postId: Int): List<Comment> =
        comments.filter { it.postId == postId }.sortedByDescending { it.createdAt }

    fun addComment(postId: Int, authorId: Int, content: String): Comment {
        val comment = Comment(nextCommentId++, postId, authorId, content)
        comments.add(comment)
        saveData()
        return comment
    }

    fun deleteComment(id: Int) {
        comments.removeIf { it.id == id }
        saveData()
    }

    private fun invalidateCache() {
        postCache.clear()
    }

    // --- Persistence ---

    private fun saveData() {
        val json = JSONObject().apply {
            put("nextUserId", nextUserId)
            put("nextCategoryId", nextCategoryId)
            put("nextPostId", nextPostId)
            put("nextCommentId", nextCommentId)
            put("users", JSONArray().also { arr ->
                users.forEach { u ->
                    arr.put(JSONObject().apply {
                        put("id", u.id)
                        put("username", u.username)
                        put("password", u.password)
                    })
                }
            })
            put("categories", JSONArray().also { arr ->
                categories.forEach { c ->
                    arr.put(JSONObject().apply {
                        put("id", c.id)
                        put("name", c.name)
                    })
                }
            })
            put("posts", JSONArray().also { arr ->
                posts.forEach { p ->
                    arr.put(JSONObject().apply {
                        put("id", p.id)
                        put("title", p.title)
                        put("content", p.content)
                        put("categoryId", p.categoryId)
                        put("authorId", p.authorId)
                        put("createdAt", p.createdAt)
                    })
                }
            })
            put("comments", JSONArray().also { arr ->
                comments.forEach { c ->
                    arr.put(JSONObject().apply {
                        put("id", c.id)
                        put("postId", c.postId)
                        put("authorId", c.authorId)
                        put("content", c.content)
                        put("createdAt", c.createdAt)
                    })
                }
            })
        }
        prefs.edit().putString("data", json.toString()).apply()
    }

    private fun loadData() {
        val jsonStr = prefs.getString("data", null) ?: return
        try {
            val json = JSONObject(jsonStr)
            nextUserId = json.optInt("nextUserId", 1)
            nextCategoryId = json.optInt("nextCategoryId", 1)
            nextPostId = json.optInt("nextPostId", 1)
            nextCommentId = json.optInt("nextCommentId", 1)

            val usersArr = json.optJSONArray("users") ?: JSONArray()
            for (i in 0 until usersArr.length()) {
                val o = usersArr.getJSONObject(i)
                users.add(User(o.getInt("id"), o.getString("username"), o.getString("password")))
            }
            val catsArr = json.optJSONArray("categories") ?: JSONArray()
            for (i in 0 until catsArr.length()) {
                val o = catsArr.getJSONObject(i)
                categories.add(Category(o.getInt("id"), o.getString("name")))
            }
            val postsArr = json.optJSONArray("posts") ?: JSONArray()
            for (i in 0 until postsArr.length()) {
                val o = postsArr.getJSONObject(i)
                posts.add(Post(
                    o.getInt("id"), o.getString("title"), o.getString("content"),
                    o.getInt("categoryId"), o.getInt("authorId"), o.getLong("createdAt")
                ))
            }
            val commentsArr = json.optJSONArray("comments") ?: JSONArray()
            for (i in 0 until commentsArr.length()) {
                val o = commentsArr.getJSONObject(i)
                comments.add(Comment(
                    o.getInt("id"), o.getInt("postId"), o.getInt("authorId"),
                    o.getString("content"), o.getLong("createdAt")
                ))
            }
        } catch (_: Exception) {
            // Corrupted data — start fresh
        }
    }
}
