package com.ngangavictor.mychat.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ngangavictor.mychat.R

class MessageHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    var textViewTime: TextView = itemView.findViewById(R.id.textViewTime) as TextView
    var textViewMessage: TextView = itemView.findViewById(R.id.textViewMessage) as TextView

}