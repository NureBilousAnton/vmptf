package com.example.pz3

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pz3.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLevel1.setOnClickListener {
            startActivity(Intent(this, Level1Activity::class.java))
        }
        binding.btnLevel2.setOnClickListener {
            startActivity(Intent(this, Level2Activity::class.java))
        }
        binding.btnLevel3.setOnClickListener {
            startActivity(Intent(this, Level3Activity::class.java))
        }
        binding.btnLevel4.setOnClickListener {
            startActivity(Intent(this, Level4Activity::class.java))
        }
    }
}
