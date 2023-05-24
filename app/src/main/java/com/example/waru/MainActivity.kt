package com.example.waru

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.waru.databinding.ActivityMainBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val calendarView: MaterialCalendarView = binding.calender

        //현재 날짜 설정 -> calender객체 만들어줌
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val today = CalendarDay.from(year, month, day)

        // 앱이 켜지면 현재 날짜로 자동 선택됨
        calendarView.selectedDate = today

        //날짜 선택 후 버튼을 누르면 sub1로 날짜 데이터가 넘어감
        binding.mainbtn.setOnClickListener {
            val selectedDate = calendarView.selectedDate?.date

            selectedDate?.let {
                val intent = Intent(this, SubActivity1::class.java)
                val calendar = Calendar.getInstance()
                calendar.time = selectedDate

                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val formattedDate = String.format(Locale.getDefault(), "%04d년 %02d월 %02d일", year, month, day)
                intent.putExtra("selectedDate", formattedDate)

                startActivity(intent)
            }

        }

    }
}