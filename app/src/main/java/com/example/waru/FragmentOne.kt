package com.example.waru

import android.annotation.SuppressLint
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.provider.BaseColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.waru.databinding.ActivitySub22Binding

class FragmentOne(private val date: String) :Fragment() {
    private lateinit var binding: ActivitySub22Binding
    private lateinit var dbHelper: Database.DbHelper
    private var isDbHelperInitialized = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ActivitySub22Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = Database.DbHelper(requireContext())  // dbHelper 초기화
        isDbHelperInitialized = true

        val loading = LoadingAni(binding.lodingTextView)
        val hasDiary = checkDiaryExists(date)
        if (hasDiary) {
            loading.stopAnimation()
            val comment = readComment(date)
            binding.lodingTextView.text = comment
        } else {
            loading.startLoding()
            startCheckingDiaryExists(loading)
        }
    }
    
    // DB에 해당 날짜의 코멘트가 저장되어 있는지 확인하는 메소드
    private fun checkDiaryExists(date: String?): Boolean {
        dbHelper = Database.DbHelper(requireContext())
        val db = dbHelper.readableDatabase

        val projection = arrayOf(BaseColumns._ID)
        val selection = "${Database.DBContract.Entry.date} = ?"
        val selectionArgs = arrayOf(date)

        val cursor = db.query(
            Database.DBContract.Entry.table_name4, projection, selection, selectionArgs, null, null, null
        )

        val hasDiary = cursor.count > 0
        cursor.close()
        return hasDiary
    }

    // checkDiaryExists 메소드를 1초 간격으로 진행해 exists가 확인되면 animation이 종료되고 코멘트가 보이도록 함.
    private fun startCheckingDiaryExists(loading: LoadingAni) {
        val handler = Handler()
        val delay = 1000L // 1초마다 체크

        handler.postDelayed(object : Runnable {
            override fun run() {
                val hasDiary = checkDiaryExists(date)
                if (hasDiary) {
                    loading.stopAnimation()
                    val comment = readComment(date)
                    binding.comment.text = comment
                } else {
                    handler.postDelayed(this, delay)
                }
            }
        }, delay)
    }

    @SuppressLint("Range")
    private fun readComment(date: String): String?{
        val db = dbHelper.readableDatabase
        var comment: String? = null

        val cursor: Cursor = db.rawQuery("SELECT comment FROM comment_result WHERE date = ?", arrayOf(date))

        if (cursor.moveToFirst()) {
            // 조회된 레코드가 있을 경우 해당 comment 값을 가져옴
            comment = cursor.getString(cursor.getColumnIndex("comment"))
        }

        cursor.close()
        db.close()

        return comment
    }
}