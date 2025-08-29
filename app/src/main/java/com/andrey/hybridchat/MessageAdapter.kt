package com.andrey.hybridchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andrey.hybridchat.models.Message
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MessageAdapter(private val messageList: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Константы для определения типа сообщения
    private val ITEM_SENT = 1
    private val ITEM_RECEIVED = 2

    // ViewHolder для отправленных сообщений
    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessageText: TextView = itemView.findViewById(R.id.sentMessageTextView)
    }

    // ViewHolder для полученных сообщений
    class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receivedMessageText: TextView = itemView.findViewById(R.id.receivedMessageTextView)
    }

    // САМЫЙ ГЛАВНЫЙ НОВЫЙ МЕТОД: getItemViewType
    // Он определяет, какое сообщение мы сейчас обрабатываем - отправленное или полученное.
    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        // Если ID отправителя совпадает с ID текущего пользователя - это отправленное сообщение.
        return if (Firebase.auth.currentUser?.uid == currentMessage.senderId) {
            ITEM_SENT
        } else {
            ITEM_RECEIVED
        }
    }

    // onCreateViewHolder теперь проверяет тип View и "раздувает" правильный макет
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            // Создаем View для отправленного сообщения
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sent_message, parent, false)
            SentViewHolder(view)
        } else {
            // Создаем View для полученного сообщения
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_received_message, parent, false)
            ReceivedViewHolder(view)
        }
    }

    // getItemCount остался таким же
    override fun getItemCount(): Int {
        return messageList.size
    }

    // onBindViewHolder теперь тоже проверяет тип и работает с правильным ViewHolder'ом
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]

        if (holder.itemViewType == ITEM_SENT) {
            // Заполняем "карточку" отправленного сообщения
            val sentHolder = holder as SentViewHolder
            sentHolder.sentMessageText.text = currentMessage.text
        } else {
            // Заполняем "карточку" полученного сообщения
            val receivedHolder = holder as ReceivedViewHolder
            receivedHolder.receivedMessageText.text = currentMessage.text
        }
    }
}