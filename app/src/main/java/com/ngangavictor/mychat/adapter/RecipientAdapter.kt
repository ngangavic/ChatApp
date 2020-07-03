package com.ngangavictor.mychat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ngangavictor.mychat.R
import com.ngangavictor.mychat.holders.RecipientHolder
import com.ngangavictor.mychat.listeners.SelectedRecipient
import com.ngangavictor.mychat.models.Recipient

class RecipientAdapter(private val email: ArrayList<Recipient>, private val userSelected: SelectedRecipient) :
        RecyclerView.Adapter<RecipientHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipientHolder {
        val viewHolder: RecipientHolder

            val layoutView =
                    LayoutInflater.from(parent.context).inflate(R.layout.row_chat_recipient, parent, false)
        viewHolder = RecipientHolder(layoutView)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return email.size
    }

    override fun onBindViewHolder(holder: RecipientHolder, position: Int) {
        holder.textViewName.text=email[position].email
        holder.textViewName.setOnClickListener {
            userSelected.setEmail(email[position].email)
            userSelected.setRecipientId(email[position].recipientId)

        }
    }
}