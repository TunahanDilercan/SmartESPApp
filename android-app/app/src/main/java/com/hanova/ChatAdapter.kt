package com.hanova

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hanova.model.Message

class ChatAdapter(private var messages: List<Message>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private var lastAnimatedPosition = -1

    fun updateMessages(newMessages: List<Message>) {
        this.messages = newMessages
        notifyDataSetChanged()
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receivedLayout: ConstraintLayout = itemView.findViewById(R.id.receivedMessageLayout)
        val receivedSender: TextView = itemView.findViewById(R.id.receivedMessageSender)
        val receivedMessage: TextView = itemView.findViewById(R.id.receivedMessageText)
        val receivedTime: TextView = itemView.findViewById(R.id.receivedMessageTime)

        val sentLayout: ConstraintLayout = itemView.findViewById(R.id.sentMessageLayout)
        val sentMessage: TextView = itemView.findViewById(R.id.sentMessageText)
        val sentTime: TextView = itemView.findViewById(R.id.sentMessageTime)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_message_item, parent, false)
        return ChatViewHolder(view)
    }    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        val isUserMessage = message.isFromUser
        
        if (isUserMessage) {
            holder.sentLayout.visibility = View.VISIBLE
            holder.receivedLayout.visibility = View.GONE
            holder.sentMessage.text = message.content
            holder.sentTime.text = message.timestamp
        } else {
            holder.receivedLayout.visibility = View.VISIBLE
            holder.sentLayout.visibility = View.GONE
            holder.receivedSender.text = message.sender
            holder.receivedMessage.text = message.content
            holder.receivedTime.text = message.timestamp
        }

        // Use holder.adapterPosition instead of position to be safer
        val adapterPosition = holder.adapterPosition
        if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition > lastAnimatedPosition) {
            animateMessage(holder.itemView, adapterPosition, isUserMessage)
            lastAnimatedPosition = adapterPosition
        }
    }

    private fun animateMessage(view: View, position: Int, isUserMessage: Boolean) {
        view.alpha = 0f

        val translationX = if (isUserMessage) 100f else -100f
        val fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        val slideIn = ObjectAnimator.ofFloat(view, "translationX", translationX, 0f)

        val animSet = AnimatorSet()
        animSet.playTogether(fadeIn, slideIn)
        animSet.duration = 300
        animSet.startDelay = position * 100L
        animSet.interpolator = DecelerateInterpolator()
        animSet.start()
    }

    override fun getItemCount() = messages.size
}
