package com.andrey.hybridchat

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
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
    private var receiverPhoneNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        receiverUid = intent.getStringExtra("USER_UID")
        val receiverEmail = intent.getStringExtra("USER_EMAIL")
        senderUid = auth.currentUser?.uid

        if (receiverUid != null) {
            fetchReceiverPhoneNumber(receiverUid!!)
        }

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

    private fun fetchReceiverPhoneNumber(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    receiverPhoneNumber = document.getString("phoneNumber")
                    Log.d("ChatActivity", "Receiver phone number loaded: $receiverPhoneNumber")
                }
            }
    }

    private fun sendMessage(messageText: String) {
        if (NetworkChecker.isNetworkAvailable(this)) {
            sendMessageFirestore(messageText, "firebase")
        } else {
            sendMessageSms(messageText)
        }
    }

    // Теперь эта функция принимает и канал отправки
    private fun sendMessageFirestore(messageText: String, channel: String) {
        val message = Message(
            text = messageText,
            senderId = senderUid,
            timestamp = System.currentTimeMillis(),
            channel = channel // Устанавливаем канал
        )
        val chatRoomId = listOfNotNull(senderUid, receiverUid).sorted().joinToString("_")

        db.collection("chats").document(chatRoomId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                // Очищаем поле ввода только если это не SMS (чтобы не было двойной очистки)
                if (channel == "firebase") {
                    messageEditText.setText("")
                }
            }
    }

    private fun sendMessageSms(messageText: String) {
        val phoneNumber = receiverPhoneNumber
        if (phoneNumber == null) {
            Toast.makeText(this, "Не удалось найти номер телефона собеседника", Toast.LENGTH_LONG).show()
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                // 1. Отправляем реальное SMS
                val smsManager: SmsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, messageText, null, null)
                Toast.makeText(this, "Сообщение отправлено по SMS", Toast.LENGTH_LONG).show()

                // 2. СРАЗУ ЖЕ дублируем его в Firestore
                sendMessageFirestore(messageText, "sms")

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