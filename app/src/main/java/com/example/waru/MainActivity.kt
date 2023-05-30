package com.example.waru

import android.content.Intent
import android.os.Bundle
import android.provider.BaseColumns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.waru.databinding.ActivityMainBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: Database.DbHelper
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
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

        //해당날짜의 일기 작성여부에 따라 버튼의 text 및 textView 변경
        calendarView.setOnDateChangedListener { _, selectedDate, _ ->
            val hasDiary = checkIfDiaryExistsForDate(selectedDate)
            if (hasDiary) {
                binding.Notmaintext.visibility= View.VISIBLE
                binding.Existmaintext.visibility=View.GONE
                binding.mainbtn.text = "일기 보러가기"
            } else {
                binding.Notmaintext.visibility= View.GONE
                binding.Existmaintext.visibility=View.VISIBLE
                binding.mainbtn.text = "일기 기록하기"
            }
        }

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

                //해당 날짜에 일기 내용이 있으면 intent로 데이터 보내줌
                val diaryContent = getDiaryContentForDate(calendarView.selectedDate)
                if (diaryContent != null) {
                    intent.putExtra("diaryContent", diaryContent)
                }

                startActivity(intent)
            }

        }

    }
    // 일기 존재 여부를 확인
    private fun checkIfDiaryExistsForDate(selectedDate: CalendarDay): Boolean {
        val selectedCalendar = selectedDate.calendar
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
        val dateString = dateFormat.format(selectedCalendar.time)

        dbHelper = Database.DbHelper(this)
        val db = dbHelper.readableDatabase

        val projection = arrayOf(BaseColumns._ID)
        val selection = "${Database.DBContract.Entry.date} = ?"
        val selectionArgs = arrayOf(dateString)

        val cursor = db.query(
            Database.DBContract.Entry.table_name1,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val hasDiary = cursor.count > 0
        cursor.close()

        return hasDiary
    }

    //해당 날짜에 일기가 있으면 일기 내용을 보내줌
    private fun getDiaryContentForDate(selectedDate: CalendarDay): String? {
        val selectedCalendar = selectedDate.calendar
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
        val dateString = dateFormat.format(selectedCalendar.time)

        dbHelper = Database.DbHelper(this)
        val db = dbHelper.readableDatabase

        val projection = arrayOf(Database.DBContract.Entry.text)
        val selection = "${Database.DBContract.Entry.date} = ?"
        val selectionArgs = arrayOf(dateString)

        val cursor = db.query(
            Database.DBContract.Entry.table_name1,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val stringBuilder = StringBuilder()

        while (cursor.moveToNext()) {
            val sentence = cursor.getString(cursor.getColumnIndexOrThrow(Database.DBContract.Entry.text))
            stringBuilder.append(sentence).append("\n")
        }

        cursor.close()

        val diaryContent = stringBuilder.toString().trimEnd()

        return if (diaryContent.isNotEmpty()) diaryContent else null
    }
}