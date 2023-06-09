package com.pinekim.usegpt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pinekim.usegpt.adapter.MessageAdapter
import com.pinekim.usegpt.databinding.ActivityChatBinding
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

class chatActivity : AppCompatActivity() {

    private var mBinding: ActivityChatBinding? = null
    private val binding get() = mBinding!!

    private val btnBackArrow by lazy { binding.btnBack }
    private val recyclerView by lazy { findViewById<RecyclerView>(R.id.recycler_view) }
    private val tvWelcome by lazy { findViewById<TextView>(R.id.tv_welcome) }
    private val etMsg by lazy { findViewById<EditText>(R.id.et_msg) }
    private val btnSend by lazy { findViewById<ImageButton>(R.id.btn_send) }

    private val messageList = mutableListOf<Message>()
    private val messageAdapter = MessageAdapter(messageList)

    private var client = OkHttpClient()

    companion object {
        val JSON = "application/json; charset=utf-8".toMediaType()
        private const val MY_SECRET_KEY = "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"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btnBackArrow.setOnClickListener {
            finish()
        }

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
        messageList.add(Message("...", Message.SENT_BY_BOT)) // 대답을 기다리는 동안 보여지는 메세지

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
            .header("Authorization", "Bearer ${chatActivity.MY_SECRET_KEY}")
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