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

    //감정분석 결과 출력
    //해당 날짜의 모든 결과가 보여지도록 수정
    //이전 감정분석 결과와 비교하여 동일하면 출력되지 않게 함 --> 한 문장의 감정분석 결과가 연속해서 출력되는 것을 막음
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

        val resultBuilder = StringBuilder()

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val scoreString =
                cursor.getString(cursor.getColumnIndexOrThrow(Database.DBContract.Entry.sentimentScore))
            val magnitudeString =
                cursor.getString(cursor.getColumnIndexOrThrow(Database.DBContract.Entry.sentimentMagnitude))
            val score = scoreString.toFloatOrNull()
            val magnitude = magnitudeString.toFloatOrNull()

            if (score != null && magnitude != null) {
                val result = "Score: $score\nMagnitude: $magnitude"
                resultBuilder.append(result).append("\n")
            }

            cursor.moveToNext()
        }

        cursor.close()

        val result = resultBuilder.toString().trim()
        return if (result.isNotEmpty()) result else null
    }

    //문장 출력
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

        val sentenceSet = mutableSetOf<String>()

        cursor.moveToFirst()
        //while문을 통해 한문장씩 출력되게 만듦
        while (!cursor.isAfterLast) {
            val sentence = cursor.getString(cursor.getColumnIndexOrThrow(Database.DBContract.Entry.text))
            sentenceSet.add(sentence)
            cursor.moveToNext()
        }

        cursor.close()

        return if (sentenceSet.isNotEmpty()) sentenceSet.joinToString("\n") else null
    }

}