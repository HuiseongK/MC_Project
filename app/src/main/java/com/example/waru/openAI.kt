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

//        val prompt = "Analyze user-provided diaries and generate responses to them.\n" +
//                "- If the content is positive, you can create a one-sentence comment, and if the content is practical, feedback the contents of the diary to generate comments of advice or encouragement.\n" +
//                "\n" +
//                "- Tone: Polite\n" +
//                "- Style: Concisely\n" +
//                "- Reader level: University students\n" +
//                "- Legnth: within 50 characters\n" +
//                "- Perspective: Advisor\n" +
//                "- Format: Print in dialog type answer format into Korean"

        val prompt = "Analyze user-provided diaries and generate responses to them.\n" +
                "- If the content is positive, you can create a one-sentence comment, and if the content is practical, feedback the contents of the diary to generate comments of advice or encouragement.\n" +
                "- If there is something about the point of time, please respond based on the standard of Seoul, Korea.\n" +
                "- All responses are based on Korea\n" +
                "\n" +
                "- Tone: Polite\n" +
                "- Style: Concisely\n" +
                "- Reader level: University students\n" +
                "- Legnth: within 50 characters\n" +
                "- Perspective: Advisor\n" +
                "- Format: Print in dialog type answer format into Korean"

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

        val url = "https://api.openai.com/v1/chat/completions"
        val authorizationHeaderValue = "Bearer $apiKey"

        // 요청 헤더 설정 & post 요청
        val request = Request.Builder()
            .url(url)
            .header("Authorization", authorizationHeaderValue)
            .header("Content-Type", "application/json")
            .post(body)
            .build()

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
