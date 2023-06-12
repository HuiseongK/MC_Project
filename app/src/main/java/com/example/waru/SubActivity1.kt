package com.example.waru

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.BaseColumns
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.example.waru.databinding.AcitivitySub1Binding
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.GenericJson
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.language.v1.CloudNaturalLanguage
import com.google.api.services.language.v1.CloudNaturalLanguageRequest
import com.google.api.services.language.v1.CloudNaturalLanguageScopes
import com.google.api.services.language.v1.model.AnalyzeSentimentRequest
import com.google.api.services.language.v1.model.AnalyzeSentimentResponse
import com.google.api.services.language.v1.model.Document
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

class SubActivity1 :AppCompatActivity() {

    // 변수 선언
    lateinit var binding: AcitivitySub1Binding
    var editTextView: EditText? = null
    private var mCredential: GoogleCredential? = null
    private var mThread: Thread? = null
    private val mRequests: BlockingQueue<CloudNaturalLanguageRequest<out GenericJson>> =
        ArrayBlockingQueue(3)
    private val mApi = CloudNaturalLanguage.Builder(
        NetHttpTransport(),
        JacksonFactory.getDefaultInstance()
    ) { request -> mCredential!!.initialize(request) }.build()
    private lateinit var dbHelper: Database.DbHelper
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    var todayDate: String = ""

    companion object {
        private const val LOADER_ACCESS_TOKEN = 1
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0,0,0,"일기 삭제")
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            0 -> {
                deleteDiary()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcitivitySub1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.date.text = intent.getStringExtra("selectedDate")

        val date = intent.getStringExtra("selectedDate")
        val text = intent.getStringExtra("diaryContent")
        if (text != null) {
            binding.daily.setText(text)
        }

        // 선택 날짜 설정
        todayDate = date.toString()

        // dbHelper 초기화
        dbHelper = Database.DbHelper(this)

        // View 초기화
        editTextView = binding.daily

        // google API 준비
        prepareApi()

        // 분석 실행 버튼 리스너 설정
        binding.save.setOnClickListener {
            startAnalysis()
        }

        // 작성된 일기가 있는지 확인하여 버튼 텍스트 설정
        val diaryExists = checkDiaryExists(date)
        if (diaryExists) {
            // 작성된 일기가 있을 경우
            binding.save.text = "수정"
        } else {
            // 작성된 일기가 없을 경우
            binding.save.text = "저장"
        }


    }

    //db에 일기가 있는지 확인
    private fun checkDiaryExists(date: String?): Boolean {
        dbHelper = Database.DbHelper(this)
        val db = dbHelper.readableDatabase

        val projection = arrayOf(BaseColumns._ID)
        val selection = "${Database.DBContract.Entry.date} = ?"
        val selectionArgs = arrayOf(date)

        val cursor = db.query(
            Database.DBContract.Entry.table_name2, projection, selection, selectionArgs, null, null, null
        )

        val hasDiary = cursor.count > 0
        cursor.close()
        return hasDiary

    }

    //일기 내용이 동일한지 확인
    private fun getDiaryContent(date: String): String? {
        dbHelper = Database.DbHelper(this)
        val db = dbHelper.readableDatabase

        val projection = arrayOf(Database.DBContract.Entry.text)
        val selection = "${Database.DBContract.Entry.date} = ?"
        val selectionArgs = arrayOf(date)

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

    //뒤로가기 버튼 누르면 MainActivitiy로
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }

    // 텍스트 감정 분석 시작
    //  kss(Korean Sentence Splitter)를 사용
    // 코루틴 사용 --> 백그라운드 스레드에서 UI 뷰에 접근
    private fun startAnalysis() {
        coroutineScope.launch(Dispatchers.Default) {
            val textToAnalyze = editTextView!!.text.toString()
            if (TextUtils.isEmpty(textToAnalyze)) {
                withContext(Dispatchers.Main) {
                    editTextView!!.error = "empty_text_error_msg"
                }
            } else {
                //UI 스레드에서 실행되도록함 --> withContext를 사용하여 비동기 작업을 순차 코드처럼 작성
                // 끝나기 전 까지 해당 코루틴 일시정지
                withContext(Dispatchers.Main) {
                    editTextView!!.error = null
                }

                val splitor = SplitSentence()
                val split = splitor.Start_Split(textToAnalyze)

                if (split.isNotEmpty()) {
                    val date = binding.date.text.toString()
                    val db = dbHelper.writableDatabase
                    val myEntry = Database.DBContract.Entry
                    // 문장별로 나누기 이전에 일기내용의 변화가 없으면 return하고 일기내용의 변화가 있으면 delete함
                    val existingDiary = getDiaryContent(date)
                    Log.d("TAG", existingDiary.toString())
                    Log.d("TAG", textToAnalyze)
                    if (existingDiary != null && existingDiary == textToAnalyze) {
                        Log.d("TAG", "일기의 변화가 없습니다")
                        proceedToSubActivity2()
                        return@launch
                    }
                    else{
                        db.delete(myEntry.table_name1, "${myEntry.date} = ?", arrayOf(date))
                        db.delete(myEntry.table_name2, "${myEntry.date} = ?", arrayOf(date))
                        db.delete(myEntry.table_name3, "${myEntry.date} = ?", arrayOf(date))
                        db.delete(myEntry.table_name4, "${myEntry.date} = ?", arrayOf(date))
                    }

                    // 요청을 한 번만 보내기 위해 sentences 리스트 생성
                    val sentences = mutableListOf<String>()

                    for (sentence in split) {
                        if (sentence.isNotEmpty()) {
                            sentences.add(sentence)
                        }
                        Log.d("SPLIT", sentence)
                    }

                    // 요청을 한 번만 보내는 로직 추가
                    // 문장 단위로 나눠준 것을 하나로 만들어서 analyzeSentiment가 한번만 실행되게 만들어줌 --> 문장수에 상관없이 요청은 한번만 발생
                    // 루프와 조건문을 추가해 한번의 요청으로 API가 문장 별로 감정분석 가능하도록 수정함.

                    var resultText = ""
                    if (sentences.isNotEmpty()) {
                        for (sentence in sentences) {
                            if(sentence.last() != '.'){ resultText += "$sentence. " }
                            else{ resultText += "$sentence " }
                        }
                        analyzeSentiment(resultText)
                    }

                }
            }
        }
    }

    private fun proceedToSubActivity2() {
        val intent = Intent(this, SubActivity2::class.java)
        intent.putExtra("selectedDate", binding.date.text.toString())
        startActivity(intent)
    }

    private fun deleteDiary() {
        val date = binding.date.text.toString()
        val dbHelper = Database.DbHelper(this)
        val db = dbHelper.writableDatabase

        val myEntry = Database.DBContract.Entry
        //해당 날짜의 일기내용 삭제
        db.delete(myEntry.table_name1, "${myEntry.date} = ?", arrayOf(date))
        db.delete(myEntry.table_name2, "${myEntry.date} = ?", arrayOf(date))
        db.delete(myEntry.table_name3, "${myEntry.date} = ?", arrayOf(date))
        db.delete(myEntry.table_name4, "${myEntry.date} = ?", arrayOf(date))

        binding.daily.setText("")

        runOnUiThread {
            Toast.makeText(this, "일기가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    //감정 분석 요청 생성
    private suspend fun analyzeSentiment(text: String?) {
        withContext(Dispatchers.Default) {
            try {
                mRequests.add(
                    mApi.documents().analyzeSentiment(
                        AnalyzeSentimentRequest().setDocument(
                            Document().setContent(text).setType("PLAIN_TEXT")
                        )
                    )
                )
                Log.d("TAG", "요청 생성 완료")
            } catch (e: IOException) {
                Log.e("tag", "Failed to create analyze request.", e)
            }
        }
    }


    // API 토큰 로드
    @Suppress("DEPRECATION")
    private fun prepareApi() {
        supportLoaderManager.initLoader(
            LOADER_ACCESS_TOKEN,
            null,
            object : LoaderManager.LoaderCallbacks<String> {
                override fun onCreateLoader(id: Int, args: Bundle?): Loader<String> {
                    return AccessTokenLoader(this@SubActivity1)
                }

                override fun onLoadFinished(loader: Loader<String>, token: String) {
                    setAccessToken(token)
                }

                override fun onLoaderReset(loader: Loader<String>) {}
            })
    }

    // 액세스 토큰 설정
    private fun setAccessToken(token: String?) {
        mCredential =
            GoogleCredential().setAccessToken(token).createScoped(CloudNaturalLanguageScopes.all())
        startWorkerThread()
    }

    // 작업 스레드 시작
    private fun startWorkerThread() {
        if (mThread != null) {
            return
        }
        mThread = Thread {
            while (true) {
                if (mThread == null) {
                    break
                }
                try {
                    deliverResponse(mRequests.take().execute())
                } catch (e: InterruptedException) {
                    Log.e("TAG", "Interrupted.", e)
                    break
                } catch (e: IOException) {
                    Log.e("TAG", "Failed to execute a request.", e)
                }
            }
        }
        mThread!!.start()
    }


    // API 응답 처리
    private fun deliverResponse(response: GenericJson) {
        Log.d("TAG", "Generic Response --> $response")
        runOnUiThread {
//            Toast.makeText(this@SubActivity1, "Response Recieved from Cloud NLP API", Toast.LENGTH_SHORT).show()
            try {
                saveResponseToInternalStorage(response)
            } catch (e: IOException) { e.printStackTrace() }
        }
    }

    //db에 일기 날짜, 일기 내용, 분석 결과(score, magnitude)를 넣어줌
    // --> 문장별로 감정이 분석되기 때문에 문장별로 날짜가 생성됨
    private fun saveResponseToDatabase(response: GenericJson) {
        val db = dbHelper.writableDatabase
        //AnalyzeSentimentResponse 형식으로 캐스팅
        val analyzeSentimentResponse = response as AnalyzeSentimentResponse
        val date = binding.date.text.toString()
        val text = binding.daily.text.toString()

        val myEntry = Database.DBContract.Entry

        // 문장별 감정 분석 결과 저장
        val sentences = analyzeSentimentResponse.sentences
        if (sentences != null && sentences.isNotEmpty()) {
            for (sentence in sentences) {
                val sentenceText = sentence.text?.content
                if (sentenceText != null && sentenceText.isNotEmpty()) {
                    val sentenceValues = ContentValues().apply {
                        put(myEntry.date, date)
                        put(myEntry.text, sentenceText)
                        put(myEntry.sentimentScore, sentence.sentiment?.score?.toFloat() ?: -1f)
                    }
                    db.insert(myEntry.table_name1, null, sentenceValues)
                }
            }
        } else {
            // 문장이 없는 경우 전체 일기를 저장
            val values = ContentValues().apply {
                put(myEntry.date, date)
                put(myEntry.text, text)
                put(
                    myEntry.sentimentScore,
                    analyzeSentimentResponse.documentSentiment?.score?.toFloat() ?: -1f
                )
            }
            db.insert(myEntry.table_name1, null, values)
        }
        Log.d("TAG", "감정 분석 결과가 DB에 저장되었습니다.")

    }

    private fun saveResponseFullToDatabase(response: GenericJson) {
        val db = dbHelper.writableDatabase
        val analyzeSentimentResponse = response as AnalyzeSentimentResponse
        val date = binding.date.text.toString()
        val text = binding.daily.text.toString()
        val myEntry = Database.DBContract.Entry

        val documentSentiment = analyzeSentimentResponse.documentSentiment
        if (documentSentiment != null) {
            val values = ContentValues().apply {
                put(myEntry.date, date)
                put(myEntry.text, text)
                put(myEntry.sentimentScore, documentSentiment.score?.toFloat() ?: -1f)
            }
            db.insert(myEntry.table_name2, null, values)
        }

        // score 값에 따라 색깔 결정
        val color = when {
            documentSentiment.score ?: 0f >= 0.25f && documentSentiment.score ?: 0f <= 1.0f -> "color1"
            documentSentiment.score ?: 0f >= -0.25f && documentSentiment.score ?: 0f < 0.25f -> "color2"
            else -> "color3"
        }
        // 색깔 데이터 저장
        val colorValues = ContentValues().apply {
            put(myEntry.date, date)
            put(myEntry.color, color)
        }
        Log.d("TAG", "색상이 DB에 저장되었습니다.")
        db.insert(myEntry.table_name3, null, colorValues)

        Log.d("TAG", "감정 전체 분석 결과가 DB에 저장되었습니다.")
    }

    // db에 저장함
    private fun saveResponseToInternalStorage(response: GenericJson) {
        saveResponseToDatabase(response)
        saveResponseFullToDatabase(response)

        // 코멘트 DB에 저장
        val open = openAI()
        val key = open.getKey(baseContext)
        val diary = readDiary(todayDate)
        open.sendChatRequest(key, todayDate, diary, dbHelper)

        // SubActivity2에서 저장된 분석 결과를 사용하기 위해 해당 날짜를 intent로 넘겨줌
        proceedToSubActivity2()
    }


    @SuppressLint("Range")
    private fun readDiary(date: String?): String {
        val db = dbHelper.readableDatabase
        var diary: String = ""

        val cursor: Cursor = db.rawQuery("SELECT text FROM table_name2 WHERE date = ?", arrayOf(date))

        if (cursor.moveToFirst()) {
            // 조회된 레코드가 있을 경우 해당 comment 값을 가져옴

            diary = cursor.getString(cursor.getColumnIndex("text"))
        }

        cursor.close()
        db.close()

        return diary
    }

}