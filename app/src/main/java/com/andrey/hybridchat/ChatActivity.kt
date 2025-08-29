package com.andrey.hybridchat

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andrey.hybridchat.models.Message
import com.andrey.hybridchat.utils.NetworkChecker
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
    // Новая переменная для хранения номера телефона собеседника
    private var receiverPhoneNumber: String? = null // TODO: Пока будет пустым

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        receiverUid = intent.getStringExtra("USER_UID")
        val receiverEmail = intent.getStringExtra("USER_EMAIL")
        senderUid = auth.currentUser?.uid

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = receiverEmail

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        messageAdapter = MessageAdapter(messageList)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        loadMessages()

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            }
        }
    }

    private fun sendMessage(messageText: String) {
        // === НАША НОВАЯ "УМНАЯ" ЛОГИКА ===
        if (NetworkChecker.isNetworkAvailable(this)) {
            // Если интернет есть, отправляем через Firestore
            sendMessageFirestore(messageText)
        } else {
            // Если интернета нет, отправляем через SMS
            sendMessageSms(messageText)
        }
    }

    private fun sendMessageFirestore(messageText: String) {
        val message = Message(
            text = messageText,
            senderId = senderUid,
            timestamp = System.currentTimeMillis()
        )
        val chatRoomId = listOfNotNull(senderUid, receiverUid).sorted().joinToString("_")

        db.collection("chats").document(chatRoomId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                messageEditText.setText("")
            }
    }

    private fun sendMessageSms(messageText: String) {
        // TODO: Замени на реальный номер для теста, например, свой собственный.
        // Позже мы будем получать этот номер из профиля пользователя.
        val phoneNumber = "+79185776814"

        // Проверяем, есть ли у нас разрешение на отправку SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val smsManager: SmsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, messageText, null, null)
                Toast.makeText(this, "Сообщение отправлено по SMS", Toast.LENGTH_LONG).show()
                messageEditText.setText("")
            } catch (e: Exception) {
                Toast.makeText(this, "Ошибка отправки SMS: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Нет разрешения на отправку SMS", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadMessages() {
        val chatRoomId = listOfNotNull(senderUid, receiverUid).sorted().joinToString("_")

        db.collection("chats").document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) { return@addSnapshotListener }
                if (snapshots != null) {
                    messageList.clear()
                    for (doc in snapshots.documents) {
                        val message = doc.toObject(Message::class.java)
                        if (message != null) { messageList.add(message) }
                    }
                    messageAdapter.notifyDataSetChanged()
                    chatRecyclerView.scrollToPosition(messageList.size - 1)
                }
            }
    }
}