package com.example.waru

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcitivitySub1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.date.text=intent.getStringExtra("selectedDate")

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
    private fun startAnalysis() {
        val textToAnalyze = editTextView!!.text.toString()
        if (TextUtils.isEmpty(textToAnalyze)) {
            editTextView!!.error = "empty_text_error_msg"
        } else {
            editTextView!!.error = null
            analyzeSentiment(textToAnalyze)
        }
    }

    // 감정 분석 요청 생성
    private fun analyzeSentiment(text: String?) {
        try {
            mRequests.add(mApi.documents().analyzeSentiment(AnalyzeSentimentRequest().setDocument(Document().setContent(text).setType("PLAIN_TEXT"))))
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
//                resultTextView!!.text = response.toPrettyString()
//                nestedScrollView!!.visibility = View.VISIBLE
                saveResponseToInternalStorage(response)
            } catch (e: IOException) { e.printStackTrace() }
        }
    }

    // 응답 저장 (DB 설정 전 임시적으로)
    // 저장되는 파일 경로 [/data/data/com.example.waru/files/]
    private fun saveResponseToInternalStorage(response: GenericJson) {
        val currentTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val filename = "${dateFormat.format(currentTime)}.txt"
        val fileContents = response.toPrettyString()

        try {
            openFileOutput(filename, Context.MODE_PRIVATE).use { fileOutputStream -> fileOutputStream.write(fileContents.toByteArray()) }
            Log.d("TAG", "응답이 내부 저장소에 저장되었습니다: $filename")

            // SubActivity2에서 저장된 분석 결과를 사용하기 위해 저장된 파일명을 intent에 추가함
            val intent = Intent(this, SubActivity2::class.java)
            intent.putExtra("filename", filename)
            startActivity(intent)

        } catch (e: IOException) { e.printStackTrace() }
    }

    companion object {
        private const val LOADER_ACCESS_TOKEN = 1
    }

}