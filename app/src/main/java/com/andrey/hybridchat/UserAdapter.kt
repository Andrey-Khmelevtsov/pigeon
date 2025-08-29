package com.andrey.hybridchat

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andrey.hybridchat.models.User

class UserAdapter(private val userList: List<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    // ViewHolder теперь ищет элемент по новому ID
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.textViewUsername)
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
        // Отображаем имя пользователя
        holder.usernameTextView.text = currentUser.username

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ChatActivity::class.java)

            // В "посылку" теперь кладем ID и ИМЯ пользователя
            intent.putExtra("USER_UID", currentUser.uid)
            intent.putExtra("USER_NAME", currentUser.username) // <-- ИЗМЕНЕНО

            context.startActivity(intent)
        }
    }
}