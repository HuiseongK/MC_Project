package com.example.waru

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.waru.databinding.ActivitySub21Binding

class FragmentOne(private val date: String?) : Fragment() {
    private lateinit var binding: ActivitySub21Binding
    private lateinit var dbHelper: Database.DbHelper
    private var isDbHelperInitialized = false
    private lateinit var adapter: MyAdapter

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

        val totalResult = getAnalysisResultFull(date)
        binding.totalresult.text= totalResult ?:"No analysis result available"

        // 수정된 getSentencesFromDatabase() 메서드 호출하여 데이터 받아옴
        adapter = MyAdapter(getSentencesFromDatabase(date))
        // RecyclerView에 어댑터 설정
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

    }

    //문장 출력
    private fun getSentencesFromDatabase(date: String?): MutableList<MyElement> {
        if (!isDbHelperInitialized) {
            dbHelper = Database.DbHelper(requireContext())
            isDbHelperInitialized = true
        }
        val db = dbHelper.readableDatabase
        val projection = arrayOf(
            Database.DBContract.Entry.text,
            Database.DBContract.Entry.sentimentScore,
            Database.DBContract.Entry.sentimentMagnitude
        )
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

        val sentenceList = mutableListOf<MyElement>()

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val sentence = cursor.getString(cursor.getColumnIndexOrThrow(Database.DBContract.Entry.text))
            val scoreString =
                cursor.getString(cursor.getColumnIndexOrThrow(Database.DBContract.Entry.sentimentScore))
            val magnitudeString =
                cursor.getString(cursor.getColumnIndexOrThrow(Database.DBContract.Entry.sentimentMagnitude))
            val score = scoreString.toFloatOrNull() ?: 0.0f
            val magnitude = magnitudeString.toFloatOrNull() ?: 0.0f

            val sentenceData = MyElement(sentence, score.toString(), magnitude.toString())
            sentenceList.add(sentenceData)

            cursor.moveToNext()
        }

        cursor.close()

        return sentenceList
    }

    //일기 전체 감정분석 결과 출력
    private fun getAnalysisResultFull(date:String?): String?{
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
            Database.DBContract.Entry.table_name2,
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

}