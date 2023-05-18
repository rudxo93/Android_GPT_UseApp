package com.pinekim.usegpt.model

class Message(
    private var message: String,
    private var sentBy: String,
) {

    companion object {
        const val SENT_BY_ME = "me"
        const val SENT_BY_BOT = "bot"
    }

    fun getMessage() = message

    fun setMessage(message: String) {
        this.message = message
    }

    fun getSentBy() = sentBy

    fun setSentBy(sentBy: String) {
        this.sentBy = sentBy
    }
}