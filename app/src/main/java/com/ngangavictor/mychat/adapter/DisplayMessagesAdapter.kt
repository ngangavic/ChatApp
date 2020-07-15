package com.ngangavictor.mychat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ngangavictor.mychat.R
import com.ngangavictor.mychat.holders.DisplayMessagesHolder
import com.ngangavictor.mychat.listeners.SelectedRecipient
import com.ngangavictor.mychat.models.DisplayMessages

class DisplayMessagesAdapter(private val displayMessages: ArrayList<DisplayMessages>,private val userSelected: SelectedRecipient) :
    RecyclerView.Adapter<DisplayMessagesHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisplayMessagesHolder {
        val viewHolder: DisplayMessagesHolder

        val layoutView =
            LayoutInflater.from(parent.context).inflate(R.layout.row_display_message, parent, false)
        viewHolder = DisplayMessagesHolder(layoutView)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return displayMessages.size
    }

    override fun onBindViewHolder(holder: DisplayMessagesHolder, position: Int) {
        holder.textViewEmail.text = displayMessages[position].email
        holder.textViewMessage.text = displayMessages[position].message
        holder.textViewDate.text = displayMessages[position].date
        holder.textViewEmail.setOnClickListener {
            userSelected.setEmail(displayMessages[position].email)
            userSelected.setRecipientId(displayMessages[position].otherUid)

        }
    }

}