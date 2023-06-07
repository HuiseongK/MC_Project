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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ActivitySub22Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = Database.DbHelper(requireContext())  // dbHelper 초기화
        isDbHelperInitialized = true

        //db에 저장되어 있을 때(분석결과 보고 다시 코멘트 보려고 할 때)는 로딩중이 뜨면 안되므로 경우 나눠줌
        //progressbar 이용하여 로딩을 만들어 openAI의 결과가 db에 저장될 때 까지 로딩중이 뜨도록 만듦 (제거)
        // textview를 animation를 사용해 DB에 저장될 때 까지 로딩화면이 출력되도록 구현함 (수정)
        val loading = LoadingAni(binding.lodingTextView)
        val hasDiary = checkDiaryExists(date)

        if (hasDiary) {
            loading.stopAnimation()
            val comment = readComment(date)
            binding.comment.text = comment
        } else {
            loading.startLoding()
            startCheckingDiaryExists(loading)
        }
//        if (hasDiary) {
//            val comment = readComment(date)
//            binding.comment.text = comment
//        } else {
            // 로딩 중 뷰 표시
//            showLoadingView()

//            Handler(Looper.getMainLooper()).postDelayed({
//                val comment = readComment(date)
//                binding.comment.text = comment
//
//                // 로딩 중 뷰 숨기기
//                hideLoadingView()
//            }, 10000) //10초 설정
//        }




    }

//    private fun showLoadingView() {
//        binding.progressBar.visibility = View.VISIBLE
//        binding.comment.visibility = View.GONE
//    }
//
//    private fun hideLoadingView() {
//        binding.progressBar.visibility = View.GONE
//        binding.comment.visibility = View.VISIBLE
//    }

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


    private fun checkDiaryExists(date: String?): Boolean {
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