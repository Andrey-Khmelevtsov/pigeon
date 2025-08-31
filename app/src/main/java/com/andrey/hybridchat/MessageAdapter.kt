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

class MessageAdapter(private val messageList: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val senderUid = Firebase.auth.currentUser?.uid

    companion object {
        private const val MSG_TYPE_SENT = 0
        private const val MSG_TYPE_RECEIVED = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return if (viewType == MSG_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sent_message, parent, false)
            MessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_received_message, parent, false)
            MessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messageList.size

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (message.senderId == senderUid) {
            MSG_TYPE_SENT
        } else {
            MSG_TYPE_RECEIVED
        }
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Для отправленных сообщений
        private val sentMessageTextView: TextView? = itemView.findViewById(R.id.sentMessageTextView)
        private val sentAttachmentImageView: ImageView? = itemView.findViewById(R.id.sentAttachmentImageView)
        private val sentMessageTimestamp: TextView? = itemView.findViewById(R.id.sentMessageTimestamp) // НОВОЕ!

        // Для полученных сообщений
        private val receivedMessageTextView: TextView? = itemView.findViewById(R.id.receivedMessageTextView)
        private val receivedAttachmentImageView: ImageView? = itemView.findViewById(R.id.receivedAttachmentImageView)
        private val receivedMessageTimestamp: TextView? = itemView.findViewById(R.id.receivedMessageTimestamp) // НОВОЕ!

        fun bind(message: Message) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTime = sdf.format(Date(message.timestamp))

            if (message.senderId == senderUid) { // Отправленное сообщение
                sentMessageTextView?.text = message.text
                sentMessageTimestamp?.text = formattedTime // Устанавливаем время

                if (message.attachmentUrl != null && message.attachmentType == "file") {
                    sentAttachmentImageView?.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(message.attachmentUrl)
                        .into(sentAttachmentImageView!!)
                    sentMessageTextView?.visibility = View.GONE // Скрываем текст, если есть файл
                } else {
                    sentAttachmentImageView?.visibility = View.GONE
                    sentMessageTextView?.visibility = View.VISIBLE
                }

            } else { // Полученное сообщение
                receivedMessageTextView?.text = message.text
                receivedMessageTimestamp?.text = formattedTime // Устанавливаем время

                if (message.attachmentUrl != null && message.attachmentType == "file") {
                    receivedAttachmentImageView?.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(message.attachmentUrl)
                        .into(receivedAttachmentImageView!!)
                    receivedMessageTextView?.visibility = View.GONE // Скрываем текст, если есть файл
                } else {
                    receivedAttachmentImageView?.visibility = View.GONE
                    receivedMessageTextView?.visibility = View.VISIBLE
                }
            }
        }
    }
}