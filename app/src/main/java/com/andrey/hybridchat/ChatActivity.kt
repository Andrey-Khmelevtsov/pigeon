package com.andrey.hybridchat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andrey.hybridchat.models.Message
import com.andrey.hybridchat.utils.NetworkChecker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

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
    private lateinit var filePickerLauncher: ActivityResultLauncher<String>
    private val storage = Firebase.storage
    private var chatRoomId: String? = null
    private val STORAGE_PERMISSION_REQUEST_CODE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val attachFileButton: ImageButton = findViewById(R.id.attachFileButton)

        filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                uploadFile(uri)
            }
        }

        attachFileButton.setOnClickListener {
            checkAndRequestStoragePermission()
        }

        receiverUid = intent.getStringExtra("USER_UID")
        val receiverName = intent.getStringExtra("USER_NAME")
        senderUid = auth.currentUser?.uid
        chatRoomId = listOfNotNull(senderUid, receiverUid).sorted().joinToString("_")

        if (receiverUid != null) {
            fetchReceiverPhoneNumber(receiverUid!!)
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = receiverName

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_call -> {
                val intent = Intent(this, CallActivity::class.java)
                intent.putExtra("CHANNEL_ID", chatRoomId)
                intent.putExtra("USER_NAME", supportActionBar?.title)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkAndRequestStoragePermission() {
        val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permissionToRequest) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permissionToRequest), STORAGE_PERMISSION_REQUEST_CODE)
        } else {
            launchFilePicker()
        }
    }

    private fun launchFilePicker() {
        if (NetworkChecker.isNetworkAvailable(this)) {
            filePickerLauncher.launch("image/*")
        } else {
            Toast.makeText(this, "Прикрепление файлов доступно только онлайн", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchFilePicker()
            } else {
                Toast.makeText(this, "Нужно разрешение для доступа к файлам", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadFile(fileUri: Uri) {
        Toast.makeText(this, "Загрузка файла...", Toast.LENGTH_SHORT).show()
        val fileName = "uploads/${System.currentTimeMillis()}"
        val storageRef = storage.reference.child(fileName)

        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    Toast.makeText(this, "Файл успешно загружен!", Toast.LENGTH_SHORT).show()
                    val fileUrl = downloadUri.toString()
                    sendMessageFirestore("Фото", "firebase", fileUrl, "