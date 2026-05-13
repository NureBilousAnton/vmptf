package com.example.pz3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pz3.databinding.ActivityLevel2Binding

class Level2Activity : AppCompatActivity() {
    private lateinit var binding: ActivityLevel2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLevel2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAdd.setOnClickListener { calculate("+") }
        binding.btnSub.setOnClickListener { calculate("-") }
        binding.btnMul.setOnClickListener { calculate("*") }
        binding.btnDiv.setOnClickListener { calculate("/") }
    }

    private fun calculate(op: String) {
        val a = binding.etNum1.text.toString().toDoubleOrNull()
        val b = binding.etNum2.text.toString().toDoubleOrNull()

        if (a == null || b == null) {
            binding.tvResult.text = "Введіть коректні числа"
            return
        }

        val result = when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> if (b != 0.0) a / b else null
            else -> null
        }

        binding.tvResult.text = if (result != null) "Результат: $result" else "Ділення на нуль!"
    }
}
