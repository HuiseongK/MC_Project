package com.example.waru

import android.annotation.SuppressLint
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.waru.databinding.ActivitySub21Binding
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

        val comment = readComment(date)
        binding.comment.text = comment
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