package com.example.waru

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.waru.databinding.AcitivitySub1Binding


class SubActivity1 :AppCompatActivity(){
    lateinit var binding: AcitivitySub1Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcitivitySub1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.date.text=intent.getStringExtra("selectedDate")
    }

    override fun onSupportNavigateUp() : Boolean {
        setResult(RESULT_OK, intent)
        finish()
        return true
    }
}