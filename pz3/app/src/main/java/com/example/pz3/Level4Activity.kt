package com.example.pz3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pz3.databinding.ActivityLevel4Binding
import com.example.pz3.databinding.ItemWorkoutBinding

data class Workout(val name: String, val durationMin: Int, val calories: Int)

class WorkoutAdapter(private val workouts: List<Workout>) :
    RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    inner class WorkoutViewHolder(private val binding: ItemWorkoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(workout: Workout) {
            binding.tvWorkoutName.text = workout.name
            binding.tvDuration.text = "${workout.durationMin} хв"
            binding.tvCalories.text = "${workout.calories} ккал"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) = holder.bind(workouts[position])

    override fun getItemCount() = workouts.size
}

class Level4Activity : AppCompatActivity() {
    private lateinit var binding: ActivityLevel4Binding
    private val workouts = mutableListOf<Workout>()
    private lateinit var adapter: WorkoutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLevel4Binding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = WorkoutAdapter(workouts)
        binding.rvWorkouts.layoutManager = LinearLayoutManager(this)
        binding.rvWorkouts.adapter = adapter

        binding.btnAddWorkout.setOnClickListener {
            val name = binding.etWorkoutName.text.toString().trim()
            val duration = binding.etDuration.text.toString().toIntOrNull()
            val calories = binding.etCalories.text.toString().toIntOrNull()

            if (name.isEmpty() || duration == null || calories == null) return@setOnClickListener

            workouts.add(Workout(name, duration, calories))
            adapter.notifyItemInserted(workouts.size - 1)
            updateStats()

            binding.etWorkoutName.text?.clear()
            binding.etDuration.text?.clear()
            binding.etCalories.text?.clear()
        }

        updateStats()
    }

    private fun updateStats() {
        val totalCalories = workouts.sumOf { it.calories }
        val totalMinutes = workouts.sumOf { it.durationMin }
        binding.tvStats.text =
            "Тренувань: ${workouts.size} | Час: $totalMinutes хв | Калорій: $totalCalories ккал"
    }
}
