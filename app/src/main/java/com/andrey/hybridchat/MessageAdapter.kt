package com.andrey.hybridchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andrey.hybridchat.models.Message
import com.bumptech.glide.Glide
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
        val attachmentImage: ImageView = itemView.findViewById(R.id.sentAttachmentImageView)
    }

    // ViewHolder для полученных
    class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.receivedMessageTextView)
        val timestampText: TextView = itemView.findViewById(R.id.receivedMessageTimestamp)
        val attachmentImage: ImageView = itemView.findViewById(R.id.receivedAttachmentImageView)
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
            sentHolder.timestampText.text = formattedTime

            // --- НОВАЯ ЛОГИКА ОТОБРАЖЕНИЯ ---
            if (currentMessage.attachmentUrl != null) {
                // Если есть вложение - показываем картинку, скрываем текст
                sentHolder.messageText.visibility = View.GONE
                sentHolder.attachmentImage.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(currentMessage.attachmentUrl)
                    .into(sentHolder.attachmentImage)
            } else {
                // Если вложения нет - показываем текст, скрываем картинку
                sentHolder.messageText.visibility = View.VISIBLE
                sentHolder.attachmentImage.visibility = View.GONE
                sentHolder.messageText.text = currentMessage.text
            }

        } else { // ITEM_RECEIVED
            val receivedHolder = holder as ReceivedViewHolder
            receivedHolder.timestampText.text = formattedTime

            // --- НОВАЯ ЛОГИКА ОТОБРАЖЕНИЯ ---
            if (currentMessage.attachmentUrl != null) {
                receivedHolder.messageText.visibility = View.GONE
                receivedHolder.attachmentImage.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(currentMessage.attachmentUrl)
                    .into(receivedHolder.attachmentImage)
            } else {
                receivedHolder.messageText.visibility = View.VISIBLE
                receivedHolder.attachmentImage.visibility = View.GONE
                receivedHolder.messageText.text = currentMessage.text
            }
        }
    }
}