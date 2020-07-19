package com.ngangavictor.mychat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ngangavictor.mychat.R
import com.ngangavictor.mychat.holders.ContactsHolder
import com.ngangavictor.mychat.listeners.SelectedRecipient
import com.ngangavictor.mychat.models.Contact

class ContactsAdapter(
    private val contact: ArrayList<Contact>,
    private val userSelected: SelectedRecipient
) : RecyclerView.Adapter<ContactsHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsHolder {
        val viewHolder: ContactsHolder

        val layoutView =
            LayoutInflater.from(parent.context).inflate(R.layout.row_contact, parent, false)
        viewHolder = ContactsHolder(layoutView)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return contact.size
    }

    override fun onBindViewHolder(holder: ContactsHolder, position: Int) {
        holder.textViewEmail.text = contact[position].email
        holder.textViewEmail.setOnClickListener {
            userSelected.setRecipientDetails(
                contact[position].email,
                contact[position].uid
            )
        }
    }


}