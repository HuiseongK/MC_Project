package com.example.waru

import android.content.ContentValues
import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.api.client.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class openAI() {

    fun getKey(context: Context): String{
        val stringBuilder = StringBuilder()

        try {
            val inputStream = context.resources.openRawResource(R.raw.openai_credential)
            val reader = BufferedReader(InputStreamReader(inputStream))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
                stringBuilder.append("\n")
            }
            reader.close()
        } catch(e: Exception){
            println("openAI : Line30");
        }

        return stringBuilder.toString()
    }


    fun sendChatCompletionRequest(apiKey: String, date: String, diary: String, dbHelper: Database.DbHelper) {
        val client = OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val url = "https://api.openai.com/v1/chat/completions"
        val authorizationHeaderValue = "Bearer $apiKey"

        // Prompt 초안
        val prompt = "---\n" +
                "아래 토픽의 내용이 긍정적이면 그에 알맞은 응답을 해주고 부정적이라면 해당 일기를 참고하여 조언이나 격려의 말을 해줘.\n" +
                "아래의 옵션들을 지켜줘.\n" +
                "\n" +
                "- Tone : 정중한\n" +
                "- Style : 간결하게\n" +
                "- Reader level : 대학생\n" +
                "- Length : 두 문장 이내로\n" +
                "- Perspective : 조언자\n" +
                "- Format : 대화문으로 출력하기\n" +
                "---"

        val message = JSONArray().put(
            JSONObject()
                .put("role", "user")
                .put("content", "$prompt\n$diary")
        )

        // 요청 본문 데이터 설정
        val requestBody = JSONObject()
            .put("model", "gpt-3.5-turbo")
            .put("messages", message)
            .toString()


        val body = RequestBody.create("application/json".toMediaTypeOrNull(), requestBody)

        // 요청 헤더 설정 & post 요청
        // authorizationHeaderValue 변수로 넣으면 요청 실패할 때가 있음
        // 요청이 계속 실패되면 OPENAI API KEY를 "Bearer: KEY" 형태로 직접 넣어주면 정상적으로 작동함
        val request = Request.Builder()
            .url(url)
            .header("Authorization", authorizationHeaderValue)
            .header("Content-Type", "application/json")
            .post(body)
            .build()

        println(request.toString())

        // 비동기적으로 요청 보내기
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                println("\n>        반응: $responseBody")

                val jsonObject = JSONObject(responseBody)
                val messageObject = jsonObject.getJSONArray("choices").getJSONObject(0).getJSONObject("message")
                val content = messageObject.getString("content")

                println(content)
                saveCommentToDB(date, content, dbHelper)
            }

            override fun onFailure(call: Call, e: IOException) {
                println("openAI API 요청 실패")
            }
        })
    }

    private fun saveCommentToDB(date: String, comment: String, dbHelper: Database.DbHelper){
        val db = dbHelper.writableDatabase
        val myEntry = Database.DBContract.Entry

        val values = ContentValues().apply {
            put(myEntry.date, date)
            put(myEntry.comment, comment)
        }

        db.insert(myEntry.table_name4, null, values)
        Log.d("TAG", "코멘트 응답이 DB에 저장되었습니다.")
    }
}
