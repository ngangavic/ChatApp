package com.ngangavictor.mychat.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ngangavictor.mychat.R

class DisplayMessagesHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    var textViewEmail: TextView = itemView.findViewById(R.id.textViewEmail) as TextView
    var textViewMessage: TextView = itemView.findViewById(R.id.textViewMessage) as TextView
    var textViewDate: TextView = itemView.findViewById(R.id.textViewDate) as TextView
}