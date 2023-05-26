package com.example.waru

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
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
import com.google.api.services.language.v1.model.Document
import java.io.IOException
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue


class SubActivity1 :AppCompatActivity(){

    // 변수 선언
    lateinit var binding: AcitivitySub1Binding
    var editTextView: EditText? = null
    private var mCredential: GoogleCredential? = null
    private var mThread: Thread? = null
    private val mRequests: BlockingQueue<CloudNaturalLanguageRequest<out GenericJson>> = ArrayBlockingQueue(3)
    private val mApi = CloudNaturalLanguage.Builder(NetHttpTransport(), JacksonFactory.getDefaultInstance()) { request -> mCredential!!.initialize(request)}.build()
    private lateinit var dbHelper: Database.DbHelper

    companion object {
        private const val LOADER_ACCESS_TOKEN = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcitivitySub1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.date.text=intent.getStringExtra("selectedDate")

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
    }

    //뒤로가기 버튼 누르면 MainActivitiy로
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }


    // 텍스트 감정 분석 시작
    // .을 기준으로 문장을 분리하여, 각 문장별로 감정 분석을 진행함
    private fun startAnalysis() {
        val textToAnalyze = editTextView!!.text.toString()
        if (TextUtils.isEmpty(textToAnalyze)) {
            editTextView!!.error = "empty_text_error_msg"
        } else {
            editTextView!!.error = null
            val sentences = textToAnalyze.split(".").map { it.trim() }
            if (sentences.isNotEmpty()) {
                for (sentence in sentences) {
                    if (sentence.isNotEmpty()) {
                        analyzeSentiment(sentence)
                    }
                }
            }
        }
    }

    // 감정 분석 요청 생성
    private fun analyzeSentiment(text: String?) {
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


    // API 토큰 로드
    @Suppress("DEPRECATION")
    private fun prepareApi() {
        supportLoaderManager.initLoader(LOADER_ACCESS_TOKEN, null, object : LoaderManager.LoaderCallbacks<String> {
            override fun onCreateLoader(id: Int, args: Bundle?): Loader<String> { return AccessTokenLoader(this@SubActivity1) }
            override fun onLoadFinished(loader: Loader<String>, token: String) { setAccessToken(token) }
            override fun onLoaderReset(loader: Loader<String>) {}
        })
    }

    // 액세스 토큰 설정
    private fun setAccessToken(token: String?) {
        mCredential = GoogleCredential().setAccessToken(token).createScoped(CloudNaturalLanguageScopes.all())
        startWorkerThread()
    }

    // 작업 스레드 시작
    private fun startWorkerThread() {
        if (mThread != null) { return }
        mThread = Thread {
            while (true) {
                if (mThread == null) { break }
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
            Toast.makeText(this@SubActivity1, "Response Recieved from Cloud NLP API", Toast.LENGTH_SHORT).show()
            try {
                //saveResponseToDatabase 메서드 호출
                saveResponseToDatabase(response)
//                resultTextView!!.text = response.toPrettyString()
//                nestedScrollView!!.visibility = View.VISIBLE
                saveResponseToInternalStorage(response)
            } catch (e: IOException) { e.printStackTrace() }
        }
    }

    //db에 일기 날짜, 일기 내용, 분석 결과(score, magnitude)를 넣어줌
    // --> 문장별로 감정이 분석되기 때문에 문장별로 날짜가 생성됨

    private fun saveResponseToDatabase(response: GenericJson) {
        val db = dbHelper.writableDatabase
        val analyzeSentimentResponse = response as com.google.api.services.language.v1.model.AnalyzeSentimentResponse

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
                        put(myEntry.sentimentMagnitude, sentence.sentiment?.magnitude?.toFloat() ?: -1f)
                    }
                    db.insert(myEntry.table_name1, null, sentenceValues)
                }
            }
        } else {
            // 문장이 없는 경우 전체 일기를 저장
            val values = ContentValues().apply {
                put(myEntry.date, date)
                put(myEntry.text, text)
                put(myEntry.sentimentScore, analyzeSentimentResponse.documentSentiment?.score?.toFloat() ?: -1f)
                put(myEntry.sentimentMagnitude, analyzeSentimentResponse.documentSentiment?.magnitude?.toFloat() ?: -1f)
            }
            db.insert(myEntry.table_name1, null, values)
        }

        Log.d("TAG", "감정 분석 결과가 DB에 저장되었습니다.")
    }

    // db에 저장함
    private fun saveResponseToInternalStorage(response: GenericJson) {
        saveResponseToDatabase(response)
        // SubActivity2에서 저장된 분석 결과를 사용하기 위해 해당 날짜를 intent로 넘겨줌
        val intent = Intent(this, SubActivity2::class.java)
        intent.putExtra("selectedDate", binding.date.text)
        startActivity(intent)

        }
    }
