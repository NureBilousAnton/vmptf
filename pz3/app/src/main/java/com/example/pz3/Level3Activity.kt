package com.example.pz3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pz3.databinding.ActivityLevel3Binding
import com.example.pz3.databinding.ItemMovieBinding

data class Movie(val name: String, val genre: String, val rating: Float)

class MovieAdapter(private val movies: List<Movie>) :
    RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    class MovieViewHolder(private val binding: ItemMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            binding.tvName.text = movie.name
            binding.tvGenre.text = movie.genre
            binding.tvRating.text = "★ ${movie.rating}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) = holder.bind(movies[position])

    override fun getItemCount() = movies.size
}

class Level3Activity : AppCompatActivity() {
    private lateinit var binding: ActivityLevel3Binding

    private val movies = mutableListOf(
        Movie("Inception", "Sci-Fi", 8.8f),
        Movie("The Godfather", "Crime", 9.2f),
        Movie("Interstellar", "Sci-Fi", 8.6f),
        Movie("The Dark Knight", "Action", 9.0f),
        Movie("Pulp Fiction", "Crime", 8.9f),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLevel3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = MovieAdapter(movies)
        binding.rvMovies.layoutManager = LinearLayoutManager(this)
        binding.rvMovies.adapter = adapter

        binding.btnAddMovie.setOnClickListener {
            val name = binding.etMovieName.text.toString().trim()
            val genre = binding.etMovieGenre.text.toString().trim()
            val rating = binding.etMovieRating.text.toString().toFloatOrNull()

            if (name.isEmpty() || genre.isEmpty() || rating == null) return@setOnClickListener

            movies.add(Movie(name, genre, rating))
            adapter.notifyItemInserted(movies.size - 1)
            binding.rvMovies.scrollToPosition(movies.size - 1)

            binding.etMovieName.text?.clear()
            binding.etMovieGenre.text?.clear()
            binding.etMovieRating.text?.clear()
        }
    }
}
