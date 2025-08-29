package com.andrey.hybridchat

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andrey.hybridchat.models.User

class UserAdapter(private val userList: List<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emailTextView: TextView = itemView.findViewById(R.id.textViewUserEmail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.user_list_item, parent, false)
        return UserViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.emailTextView.text = currentUser.email

        // ----- НОВЫЙ КОД ЗДЕСЬ -----
        // Устанавливаем слушателя на весь элемент списка (на всю "карточку")
        holder.itemView.setOnClickListener {
            // Получаем context (информацию о текущем экране) из самого элемента
            val context = holder.itemView.context
            // Создаем намерение открыть ChatActivity
            val intent = Intent(context, ChatActivity::class.java)

            // Кладем в "посылку" (intent) ID и email пользователя, на которого мы нажали.
            // Это нужно, чтобы ChatActivity знал, с кем именно мы хотим общаться.
            intent.putExtra("USER_UID", currentUser.uid)
            intent.putExtra("USER_EMAIL", currentUser.email)

            // Запускаем новый экран
            context.startActivity(intent)
        }
        // ----- КОНЕЦ НОВОГО КОДА -----
    }
}