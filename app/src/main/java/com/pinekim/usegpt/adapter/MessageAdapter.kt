package com.pinekim.usegpt.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pinekim.usegpt.R
import com.pinekim.usegpt.model.Message


class MessageAdapter(messageList: List<Message>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    var messageList: List<Message>

    init {
        this.messageList = messageList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, null)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message: Message = messageList[position]
        if (message.getSentBy().equals(Message.SENT_BY_ME)) {
            holder.left_chat_view.visibility = View.GONE
            holder.right_chat_view.visibility = View.VISIBLE
            holder.right_chat_tv.setText(message.getMessage())
        } else {
            holder.right_chat_view.visibility = View.GONE
            holder.left_chat_view.visibility = View.VISIBLE
            holder.left_chat_tv.setText(message.getMessage())
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var left_chat_view: LinearLayout
        var right_chat_view: LinearLayout
        var left_chat_tv: TextView
        var right_chat_tv: TextView

        init {
            left_chat_view = itemView.findViewById<LinearLayout>(R.id.left_chat_view)
            right_chat_view = itemView.findViewById<LinearLayout>(R.id.right_chat_view)
            left_chat_tv = itemView.findViewById<TextView>(R.id.left_chat_tv)
            right_chat_tv = itemView.findViewById<TextView>(R.id.right_chat_tv)
        }
    }
}