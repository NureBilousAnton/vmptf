package com.example.pz3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pz3.databinding.ActivityLevel1Binding

class Level1Activity : AppCompatActivity() {
    private lateinit var binding: ActivityLevel1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLevel1Binding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
