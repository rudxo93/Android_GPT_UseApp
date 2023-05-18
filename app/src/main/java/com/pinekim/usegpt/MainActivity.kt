package com.pinekim.usegpt

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pinekim.usegpt.adapter.MessageAdapter
import com.pinekim.usegpt.model.Message
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    val recyclerView by lazy { findViewById<RecyclerView>(R.id.recycler_view) }
    val tvWelcome by lazy { findViewById<TextView>(R.id.tv_welcome) }
    val etMsg by lazy { findViewById<EditText>(R.id.et_msg) }
    val btnSend by lazy { findViewById<ImageButton>(R.id.btn_send) }

    val messageList = mutableListOf<Message>()
    val messageAdapter = MessageAdapter(messageList)

    var client = OkHttpClient()

    companion object {
        val JSON = "application/json; charset=utf-8".toMediaType()
        private const val MY_SECRET_KEY = "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"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = messageAdapter

        btnSend.setOnClickListener {
            val question = etMsg.text.toString().trim()
            addToChat(question, Message.SENT_BY_ME)
            etMsg.setText("")
            callAPI(question)
            tvWelcome.visibility = View.GONE
        }

        client = OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun addToChat(message: String, sentBy: String) {
        runOnUiThread {
            messageList.add(Message(message, sentBy))
            messageAdapter.notifyDataSetChanged()
            recyclerView.smoothScrollToPosition(messageAdapter.itemCount - 1)
        }
    }

    fun addResponse(response: String) {
        messageList.removeAt(messageList.size - 1)
        addToChat(response, Message.SENT_BY_BOT)
    }

    fun callAPI(question: String) {
        // okhttp
        messageList.add(Message("...", Message.SENT_BY_BOT))

        val arr = JSONArray()
        val baseAi = JSONObject()
        val userMsg = JSONObject()
        try {
            //AI 속성설정
            baseAi.put("role", "user")
            baseAi.put("content", "You are a helpful and kind AI Assistant.")
            //유저 메세지
            userMsg.put("role", "user")
            userMsg.put("content", question)
            //array로 담아서 한번에 보낸다
            arr.put(baseAi)
            arr.put(userMsg)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }

        val jsonObject = JSONObject()
        try {
            jsonObject.put("model", "text-davinci-003")
            jsonObject.put("prompt", question)
            jsonObject.put("max_tokens", 4000)
            jsonObject.put("temperature", 0)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, jsonObject.toString())
        val request = Request.Builder()
            .url("https://api.openai.com/v1/completions")
            .header("Authorization", "Bearer $MY_SECRET_KEY")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                addResponse("Failed to load response due to ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    var jsonObject: JSONObject? = null
                    try {
                        jsonObject = JSONObject(response.body!!.string())
                        val jsonArray = jsonObject!!.getJSONArray("choices")
                        val result = jsonArray.getJSONObject(0).getString("text")
                        addResponse(result.trim())
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    addResponse("Failed to load response due to ${response.body!!.string()}")
                }
            }
        })
    }
}