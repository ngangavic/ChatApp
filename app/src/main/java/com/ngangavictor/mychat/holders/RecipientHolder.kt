package com.ngangavictor.mychat.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ngangavictor.mychat.R

class RecipientHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
    val textViewName: TextView =itemView.findViewById(R.id.textViewName) as TextView
}