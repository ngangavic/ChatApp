package com.ngangavictor.mychat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ngangavictor.mychat.R
import com.ngangavictor.mychat.holders.ContactSearchHolder
import com.ngangavictor.mychat.listeners.SelectedContact
import com.ngangavictor.mychat.models.ContactSearch

class ContactSearchAdapter(
    private val contactSearch: ArrayList<ContactSearch>,
    private val emailSelected: SelectedContact
) : RecyclerView.Adapter<ContactSearchHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactSearchHolder {
        val viewHolder: ContactSearchHolder

        val layoutView =
            LayoutInflater.from(parent.context).inflate(R.layout.row_chat_recipient, parent, false)
        viewHolder = ContactSearchHolder(layoutView)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return contactSearch.size
    }

    override fun onBindViewHolder(holder: ContactSearchHolder, position: Int) {
        holder.textViewName.text = contactSearch[position].email
        holder.textViewName.setOnClickListener {
            emailSelected.chosenEmail(contactSearch[position].email)
        }
    }

}