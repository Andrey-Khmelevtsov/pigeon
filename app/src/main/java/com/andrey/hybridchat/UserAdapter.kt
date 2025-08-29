package com.andrey.hybridchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andrey.hybridchat.models.User

// 1. Адаптер принимает на вход список пользователей, которых нужно отобразить
class UserAdapter(private val userList: List<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    // 2. ViewHolder: Внутренний класс, который "держит" в себе элементы одной ячейки (нашей "карточки")
    // Он знает, где на карточке находится поле для email.
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emailTextView: TextView = itemView.findViewById(R.id.textViewUserEmail)
    }

    // 3. onCreateViewHolder: Вызывается, когда RecyclerView нужна новая "карточка" для отображения.
    // Он берет наш XML-макет (user_list_item) и создает из него View.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.user_list_item, parent, false)
        return UserViewHolder(itemView)
    }

    // 4. getItemCount: Просто возвращает общее количество элементов в нашем списке.
    // RecyclerView спрашивает: "Сколько всего будет карточек?". Мы отвечаем.
    override fun getItemCount(): Int {
        return userList.size
    }

    // 5. onBindViewHolder: Самый главный метод. Вызывается для каждой видимой "карточки".
    // Его задача — взять данные из списка (по позиции) и вставить их в "карточку".
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        // Берем пользователя из списка по его номеру (позиции)
        val currentUser = userList[position]
        // Устанавливаем его email в TextView на "карточке"
        holder.emailTextView.text = currentUser.email
    }
}