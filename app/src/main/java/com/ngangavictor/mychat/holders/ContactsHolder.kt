package com.ngangavictor.mychat.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ngangavictor.mychat.R

class ContactsHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    val textViewEmail: TextView = itemView.findViewById(R.id.textViewEmail) as TextView
}