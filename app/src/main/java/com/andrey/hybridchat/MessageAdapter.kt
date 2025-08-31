package com.andrey.hybridchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andrey.hybridchat.models.Message
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(private val messageList: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_SENT = 1
    private val ITEM_RECEIVED = 2

    // ViewHolder для отправленных
    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.sentMessageTextView)
        val timestampText: TextView = itemView.findViewById(R.id.sentMessageTimestamp)
    }

    // ViewHolder для полученных
    class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.receivedMessageTextView)
        val timestampText: TextView = itemView.findViewById(R.id.receivedMessageTimestamp)
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if (Firebase.auth.currentUser?.uid == currentMessage.senderId) {
            ITEM_SENT
        } else {
            ITEM_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sent_message, parent, false)
            SentViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_received_message, parent, false)
            ReceivedViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime = sdf.format(Date(currentMessage.timestamp))

        if (holder.itemViewType == ITEM_SENT) {
            val sentHolder = holder as SentViewHolder
            sentHolder.messageText.text = currentMessage.text
            sentHolder.timestampText.text = formattedTime
        } else {
            val receivedHolder = holder as ReceivedViewHolder
            receivedHolder.messageText.text = currentMessage.text
            receivedHolder.timestampText.text = formattedTime
        }
    }
}