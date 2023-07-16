package com.inkrodriguez.notfall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.inkrodriguez.notfall.R
import com.inkrodriguez.notfall.data.Message

class MyAdapter(private val messagesList: List<Message>?) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messagesList?.get(position)
        if (message != null) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int {
        return messagesList?.size ?: 0
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tvName)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)

        fun bind(message: Message) {
            // Atualize as views do item do RecyclerView com os dados da mensagem
            tvUsername.text = message.sender
            tvMessage.text = message.message
        }
    }

}
