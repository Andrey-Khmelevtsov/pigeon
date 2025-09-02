package com.andrey.hybridchat

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig

class CallActivity : AppCompatActivity() {

    private val appId = "cd84d986dc0f48528c3139c74ece2695" // НЕ ЗАБУДЬ ВСТАВИТЬ СВОЙ КЛЮЧ
    private var channelName: String? = null
    private var token: String? = null


    private var rtcEngine: RtcEngine? = null
    private val PERMISSION_REQ_ID_RECORD_AUDIO = 22

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        // Получаем данные из Intent
        val isIncomingCall = intent.getBooleanExtra("IS_INCOMING_CALL", false)
        channelName = intent.getStringExtra("CHANNEL_ID")
        val userName = intent.getStringExtra("USER_NAME")

        val callStatusTextView: TextView = findViewById(R.id.callStatusTextView)
        val hangUpButton: ImageButton = findViewById(R.id.hangUpButton)

        if (isIncomingCall) {
            // Если это ВХОДЯЩИЙ звонок
            callStatusTextView.text = "Входящий звонок от\n$userName"
            // TODO: Здесь в будущем нужно будет показать кнопки "Принять/Отклонить" и включить рингтон
        } else {
            // Если это ИСХОДЯЩИЙ звонок (старая логика)
            callStatusTextView.text = "Звонок для\n$userName"
        }

        hangUpButton.setOnClickListener {
            leaveChannel()
        }

        // Подключаемся к каналу, только если это ИСХОДЯЩИЙ звонок
        // (для входящего нужно будет подключаться после нажатия кнопки "Принять")
        if (!isIncomingCall) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)) {
                initializeAndJoinChannel()
            }
        }
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_ID_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeAndJoinChannel()
            } else {
                Toast.makeText(this, "Нужно разрешение на микрофон для звонков", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun initializeAndJoinChannel() {
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            rtcEngine = RtcEngine.create(config)
        } catch (e: Exception) {
            throw RuntimeException("Check the error.")
        }

        // ----- НОВЫЕ СТРОКИ ЗДЕСЬ -----
        // 1. Устанавливаем профиль канала как "общение" (звонок один на один)
        rtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
        // 2. Включаем громкую связь по умолчанию
        rtcEngine?.setDefaultAudioRoutetoSpeakerphone(true)
        // 3. Гарантируем, что наш микрофон не выключен
        rtcEngine?.muteLocalAudioStream(false)
        // ----- КОНЕЦ НОВЫХ СТРОК -----

        // Подключаемся к каналу
        rtcEngine?.joinChannel(token, channelName, "", 0)
    }

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                findViewById<TextView>(R.id.callStatusTextView).text = "Собеседник в сети"
            }
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            runOnUiThread {
                Toast.makeText(applicationContext, "Успешно подключен к каналу", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                findViewById<TextView>(R.id.callStatusTextView).text = "Собеседник отключился"
                leaveChannel()
            }
        }
    }

    private fun leaveChannel() {
        rtcEngine?.leaveChannel()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        rtcEngine?.leaveChannel()
        RtcEngine.destroy()
    }
}