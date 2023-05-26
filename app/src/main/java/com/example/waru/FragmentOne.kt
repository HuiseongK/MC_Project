package com.example.waru

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.waru.databinding.ActivitySub21Binding

class FragmentOne(private val date: String?) : Fragment() {
    private lateinit var binding: ActivitySub21Binding
    private lateinit var dbHelper: Database.DbHelper
    private var isDbHelperInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivitySub21Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = Database.DbHelper(requireContext())  // dbHelper 초기화
        isDbHelperInitialized = true
        val result = getAnalysisResultFromDatabase(date)
        binding.result.text = result ?: "No analysis result available"

        val sentences = getSentencesFromDatabase(date)
        binding.sentence.text = sentences ?: "No sentences available"
    }

    private fun getAnalysisResultFromDatabase(date: String?): String? {
        if (!isDbHelperInitialized) {
            dbHelper = Database.DbHelper(requireContext())
            isDbHelperInitialized = true
        }
        val db = dbHelper.readableDatabase
        val projection = arrayOf(Database.DBContract.Entry.sentimentScore, Database.DBContract.Entry.sentimentMagnitude)
        val selection = "${Database.DBContract.Entry.date} = ?"

        // null 값인 경우 대체 값으로 처리
        val selectionArgs = arrayOf(date ?: "")

        val cursor = db.query(
            Database.DBContract.Entry.table_name1,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        var result: String? = null
        if (cursor.moveToFirst()) {
            val scoreString =
                cursor.getString(cursor.getColumnIndexOrThrow(Database.DBContract.Entry.sentimentScore))
            val magnitudeString =
                cursor.getString(cursor.getColumnIndexOrThrow(Database.DBContract.Entry.sentimentMagnitude))
            val score = scoreString.toFloatOrNull()
            val magnitude = magnitudeString.toFloatOrNull()
            if (score != null && magnitude != null) {
                result = "Score: $score\nMagnitude: $magnitude"
            }
        }

        cursor.close()
        return result
    }

    private fun getSentencesFromDatabase(date: String?): String? {
        if (!isDbHelperInitialized) {
            dbHelper = Database.DbHelper(requireContext())
            isDbHelperInitialized = true
        }
        val db = dbHelper.readableDatabase
        val projection = arrayOf(Database.DBContract.Entry.text)
        val selection = "${Database.DBContract.Entry.date} = ?"
        val selectionArgs = arrayOf(date ?: "")
        val cursor = db.query(
            Database.DBContract.Entry.table_name1,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val sentenceSet = mutableSetOf<String>() // 중복 제거를 위한 Set 사용

        if (cursor.moveToFirst()) {
            do {
                val sentence =
                    cursor.getString(cursor.getColumnIndexOrThrow(Database.DBContract.Entry.text))
                sentenceSet.add(sentence)
            } while (cursor.moveToNext())
        }

        cursor.close()

        if (sentenceSet.isNotEmpty()) {
            return sentenceSet.joinToString("\n")
        }

        return null
    }

}