package com.ngangavictor.mychat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var textViewReceiver:TextView
    private lateinit var editTextMessage:EditText
    private lateinit var imageButtonSend:ImageButton
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var recyclerViewMessages: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textViewReceiver=findViewById(R.id.textViewReceiver)
        recyclerViewMessages=findViewById(R.id.recyclerViewMessages)
        floatingActionButton=findViewById(R.id.floatingActionButton)
        imageButtonSend=findViewById(R.id.imageButtonSend)
        editTextMessage=findViewById(R.id.editTextMessage)
    }
}