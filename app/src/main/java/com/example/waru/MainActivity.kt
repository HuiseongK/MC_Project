package com.example.waru

import android.content.Intent
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.ContactsContract.Data
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
        binding.colortext.setOnClickListener {
            binding.colortext.visibility=View.GONE
        }

        // gpt test
//        val testbtn = binding.test
//        val open = openAI()
//        testbtn.setOnClickListener{
//            val key = open.getKey(baseContext)
//            open.sendChatCompletionRequest(key)
//        }

        //현재 날짜 설정 -> calender객체 만들어줌
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val today = CalendarDay.from(year, month, day)

        // 앱이 켜지면 현재 날짜로 자동 선택됨
        calendarView.selectedDate = today
        val hasDiaryToday = checkIfDiaryExistsForDate(today)
        if (hasDiaryToday) {
            binding.Notmaintext.visibility= View.GONE
            binding.Existmaintext.visibility=View.VISIBLE
            val score = getScore(today)
            binding.Existmaintext.text=getString(R.string.exist,score.toString())
            binding.mainbtn.text = "일기 보러가기"
        } else {
            binding.Notmaintext.visibility= View.VISIBLE
            binding.Existmaintext.visibility=View.GONE
            binding.mainbtn.text = "일기 기록하기"
        }


        //해당날짜의 일기 작성여부에 따라 버튼의 text 및 textView 변경
        calendarView.setOnDateChangedListener { _, selectedDate, _ ->
            val hasDiary = checkIfDiaryExistsForDate(selectedDate)
            if (hasDiary) {
                binding.Notmaintext.visibility= View.GONE
                binding.Existmaintext.visibility=View.VISIBLE
                val score = getScore(selectedDate)
                binding.Existmaintext.text=getString(R.string.exist,score.toString())
                binding.mainbtn.text = "일기 보러가기"
            } else {
                binding.Notmaintext.visibility= View.VISIBLE
                binding.Existmaintext.visibility=View.GONE
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
        // setDateColorForDiaryDates() 메서드 호출하여 일기 작성된 날짜의 색상을 설정
        setDateColorForDiaryDates()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0,0,0,"색상 설명")
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            0 -> {
                if (binding.colortext.visibility == View.VISIBLE) {
                    binding.colortext.visibility = View.GONE
                } else {
                    binding.colortext.visibility = View.VISIBLE
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
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
            Database.DBContract.Entry.table_name1, projection, selection, selectionArgs, null, null, null
        )

        val hasDiary = cursor.count > 0
        cursor.close()

        return hasDiary
    }

    private fun getScore(selectedDate: CalendarDay): Float? {
        val selectedCalendar = selectedDate.calendar
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
        val dateString = dateFormat.format(selectedCalendar.time)

        dbHelper = Database.DbHelper(this)
        val db = dbHelper.readableDatabase

        val projection = arrayOf(Database.DBContract.Entry.sentimentScore)
        val selection = "${Database.DBContract.Entry.date} = ?"
        val selectionArgs = arrayOf(dateString)

        val cursor = db.query(
            Database.DBContract.Entry.table_name2, projection, selection, selectionArgs, null, null, null
        )

        val score: Float?

        //cursor.getFloat()을 이용하여 sentimentScore값을 가져옴
        if (cursor.moveToFirst()) {
            score = cursor.getFloat(cursor.getColumnIndexOrThrow(Database.DBContract.Entry.sentimentScore))
        } else {
            score = null
        }

        cursor.close()
        return score
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
            Database.DBContract.Entry.table_name2, projection, selection, selectionArgs, null, null, null
        )

        val stringBuilder = StringBuilder()

        while (cursor.moveToNext()) {
            val sentence = cursor.getString(cursor.getColumnIndexOrThrow(Database.DBContract.Entry.text))
            stringBuilder.append(sentence).append(" ")
        }

        cursor.close()

        val diaryContent = stringBuilder.toString().trimEnd()

        return if (diaryContent.isNotEmpty()) diaryContent else null
    }

    //DB에 저장된 결과를 바탕으로, 일기가 저장되어 있으면 자동으로 db에 저장된 색상으로 변경시켜줌
    private fun setDateColorForDiaryDates() {
        dbHelper = Database.DbHelper(this)
        val db = dbHelper.readableDatabase

        val projection = arrayOf(Database.DBContract.Entry.date, Database.DBContract.Entry.color)
        val sortOrder: String? = null

        val cursor = db.query(
            Database.DBContract.Entry.table_name3, projection, null, null, null, null, sortOrder
        )

        val decorators = mutableListOf<EventDecorator>()

        while (cursor.moveToNext()) {
            val dateString =
                cursor.getString(cursor.getColumnIndexOrThrow(Database.DBContract.Entry.date))
            val color =
                cursor.getString(cursor.getColumnIndexOrThrow(Database.DBContract.Entry.color))

            val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.time = dateFormat.parse(dateString)

            val selectedDate = CalendarDay.from(
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
            )

            val colorResourceId = getColorResource(color)
            if (colorResourceId != null) {
                //EventDecorator 객체를 생성할 때 this를 사용하여 액티비티의 Context를 전달
                decorators.add(EventDecorator(this, setOf(selectedDate), colorResourceId))
            }
        }

        cursor.close()
        //일기 삭제 후 다시 main으로 돌아왔을 때 해당 날짜의 색상을 삭제하기 위해 기존의 것 remove해줌
        binding.calender.removeDecorators()
        binding.calender.addDecorators(decorators)
    }

    private fun getColorResource(color: String?): Int {
        return when (color) {
            "color1" -> R.color.purple_200
            "color2" -> R.color.purple_500
            "color3" -> R.color.purple_700
            else -> R.color.black
        }
    }

    //main돌아왔을 때마다 다시 setDateColorForDiaryDates()를 실행시킴 -> 일기 삭제 or 수정되는 경우를 반영하기 위해
    override fun onResume() {
        super.onResume()
        setDateColorForDiaryDates()
    }

}