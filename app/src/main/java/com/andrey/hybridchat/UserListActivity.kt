package com.andrey.hybridchat

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.andrey.hybridchat.models.User

class UserListActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        // 1. Находим наш RecyclerView в макете
        val userRecyclerView: RecyclerView = findViewById(R.id.userRecyclerView)

        // 2. Настраиваем RecyclerView:
        //    - Создаем адаптер с пока еще пустым списком
        userAdapter = UserAdapter(userList)
        //    - Устанавливаем LayoutManager, который говорит, как располагать элементы (просто списком)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        //    - Подключаем наш адаптер к RecyclerView
        userRecyclerView.adapter = userAdapter

        // 3. Вызываем функцию для загрузки пользователей
        fetchUsers()
    }

    private fun fetchUsers() {
        // 4. Обращаемся к коллекции "users" в Firestore
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                // 5. В случае успеха, очищаем старый список (на всякий случай)
                userList.clear()
                // 6. Проходим по каждому документу, который мы получили
                for (document in result) {
                    // 7. Превращаем документ в наш объект класса User
                    val user = document.toObject(User::class.java)

                    // 8. ВАЖНО: Добавляем пользователя в список, только если это не мы сами
                    if (user.uid != auth.currentUser?.uid) {
                        userList.add(user)
                    }
                }
                // 9. Сообщаем адаптеру, что данные изменились, и ему нужно перерисовать список
                userAdapter.notifyDataSetChanged()
                Log.d("UserListActivity", "Users loaded: ${userList.size}")
            }
            .addOnFailureListener { exception ->
                // В случае ошибки, выводим ее в лог
                Log.w("UserListActivity", "Error getting documents: ", exception)
            }
    }
}