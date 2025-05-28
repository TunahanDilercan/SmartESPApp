package com.hanova.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hanova.R
import com.hanova.model.Message

class MessagesAdapter : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    private var messages: List<Message> = listOf()

    fun updateMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        if (message.isFromUser) {
            // Kullanıcı mesajı
            holder.sentLayout.visibility = View.VISIBLE
            holder.receivedLayout.visibility = View.GONE

            holder.sentMessage.text = message.content
            holder.sentTime.text = message.timestamp
        } else {
            // Sistem/ESP8266 mesajı
            holder.receivedLayout.visibility = View.VISIBLE
            holder.sentLayout.visibility = View.GONE

            holder.receivedMessage.text = message.content
            holder.receivedSender.text = message.sender
            holder.receivedTime.text = message.timestamp
        }
    }

    override fun getItemCount() = messages.size

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sentLayout: ConstraintLayout = view.findViewById(R.id.sentMessageLayout)
        val receivedLayout: ConstraintLayout = view.findViewById(R.id.receivedMessageLayout)

        val sentMessage: TextView = view.findViewById(R.id.sentMessageText)
        val sentTime: TextView = view.findViewById(R.id.sentMessageTime)

        val receivedMessage: TextView = view.findViewById(R.id.receivedMessageText)
        val receivedSender: TextView = view.findViewById(R.id.receivedMessageSender)
        val receivedTime: TextView = view.findViewById(R.id.receivedMessageTime)
    }
}
