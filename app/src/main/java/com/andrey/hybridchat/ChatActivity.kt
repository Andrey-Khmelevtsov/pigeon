package com.andrey.hybridchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andrey.hybridchat.models.Message
import com.andrey.hybridchat.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<Message>()

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private var receiverUid: String? = null
    private var senderUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Получаем UID'ы отправителя и получателя
        receiverUid = intent.getStringExtra("USER_UID")
        val receiverEmail = intent.getStringExtra("USER_EMAIL")
        senderUid = auth.currentUser?.uid

        // Настраиваем Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = receiverEmail

        // Находим View-элементы
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        // Настраиваем RecyclerView
        messageAdapter = MessageAdapter(messageList)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        // Загружаем сообщения
        loadMessages()

        // Устанавливаем слушателя на кнопку "Send"
        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            }
        }
    }

    private fun sendMessage(messageText: String) {
        // Создаем объект сообщения
        val message = Message(
            text = messageText,
            senderId = senderUid,
            timestamp = System.currentTimeMillis()
        )

        // Создаем уникальный ID для чат-комнаты, чтобы он был одинаковым для обоих пользователей
        val chatRoomId = listOfNotNull(senderUid, receiverUid).sorted().joinToString("_")

        // Сохраняем сообщение в Firestore
        db.collection("chats").document(chatRoomId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                // Очищаем поле ввода после успешной отправки
                messageEditText.setText("")
            }
    }

    private fun loadMessages() {
        // Создаем уникальный ID для чат-комнаты
        val chatRoomId = listOfNotNull(senderUid, receiverUid).sorted().joinToString("_")

        // Устанавливаем "слушателя" на коллекцию сообщений в реальном времени
        db.collection("chats").document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING) // Сортируем по времени
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    // Обработка ошибки
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    messageList.clear() // Очищаем старый список
                    // Добавляем все новые сообщения в список
                    for (doc in snapshots.documents) {
                        val message = doc.toObject(Message::class.java)
                        if (message != null) {
                            messageList.add(message)
                        }
                    }
                    // Сообщаем адаптеру, что данные обновились
                    messageAdapter.notifyDataSetChanged()
                    // Прокручиваем список к последнему сообщению
                    chatRecyclerView.scrollToPosition(messageList.size - 1)
                }
            }
    }
}