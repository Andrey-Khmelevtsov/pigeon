package com.andrey.hybridchat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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

    // ... все старые переменные ...
    private lateinit var filePickerLauncher: ActivityResultLauncher<String>

    // НОВЫЙ КОД: Код-запрос для разрешения
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
            // ИЗМЕНЕНИЕ ЗДЕСЬ: Сначала проверяем разрешение, потом открываем галерею
            checkAndRequestStoragePermission()
        }

        // ... остальной код onCreate без изменений ...
    }

    // НОВАЯ ФУНКЦИЯ для проверки и запроса разрешения
    private fun checkAndRequestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Если разрешения нет, запрашиваем его
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
        } else {
            // Если разрешение уже есть, сразу открываем галерею
            launchFilePicker()
        }
    }

    // НОВАЯ ФУНКЦИЯ для запуска выбора файла
    private fun launchFilePicker() {
        if (NetworkChecker.isNetworkAvailable(this)) {
            filePickerLauncher.launch("*/*")
        } else {
            Toast.makeText(this, "Прикрепление файлов доступно только онлайн", Toast.LENGTH_SHORT).show()
        }
    }

    // Этот метод вызывается после того, как пользователь ответил на запрос
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Если разрешение дали, открываем галерею
                launchFilePicker()
            } else {
                // Если отказали, показываем сообщение
                Toast.makeText(this, "Нужно разрешение для доступа к файлам", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- все остальные функции (uploadFile, sendMessage и т.д.) остаются без изменений ---
    // ... (вставь сюда полный код всех остальных функций из прошлой версии) ...
}