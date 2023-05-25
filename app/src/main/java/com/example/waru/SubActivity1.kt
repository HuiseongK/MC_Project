package com.example.waru

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AppCompatActivity
import com.example.waru.databinding.AcitivitySub1Binding


class SubActivity1 :AppCompatActivity(){
    lateinit var binding: AcitivitySub1Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcitivitySub1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.date.text=intent.getStringExtra("selectedDate")

        binding.save.setOnClickListener {
            val intent = Intent(this, SubActivity2::class.java)
            startActivity(intent)
        }
    }

    //뒤로가기 버튼 누르면 MainActivitiy로
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }
}