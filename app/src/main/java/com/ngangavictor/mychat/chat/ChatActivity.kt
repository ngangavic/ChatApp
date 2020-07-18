package com.ngangavictor.mychat.chat

import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ngangavictor.mychat.R
import com.ngangavictor.mychat.adapter.MessagesAdapter
import com.ngangavictor.mychat.main.TabbedActivity
import com.ngangavictor.mychat.models.Message
import com.ngangavictor.mychat.models.MessageStructure
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity() {

    private lateinit var editTextMessage: EditText
    private lateinit var imageViewBack: ImageView
    private lateinit var textViewEmail: TextView
    private lateinit var imageButtonSend: ImageButton
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var progressBarChats: ProgressBar
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    lateinit var messagesList: MutableList<Message>
    lateinit var messagesAdapter: MessagesAdapter
    lateinit var receiverId: String
    lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        editTextMessage = findViewById(R.id.editTextMessage)
        imageButtonSend = findViewById(R.id.imageButtonSend)
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages)
        progressBarChats = findViewById(R.id.progressBarChats)

        recyclerViewMessages.layoutManager = LinearLayoutManager(this)
        recyclerViewMessages.setHasFixedSize(true)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        messagesList = ArrayList()

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.custom_chat_action_bar)
        supportActionBar?.elevation = 0F
        val view = supportActionBar?.customView
        textViewEmail = view!!.findViewById(R.id.textViewEmail)
        imageViewBack = view.findViewById(R.id.imageViewBack)

        imageViewBack.setOnClickListener {
            startActivity(Intent(this, TabbedActivity::class.java))
            finish()
        }

        imageButtonSend.setOnClickListener { sendMessage() }
    }

    override fun onStart() {
        if (intent.getStringExtra("receiverId").toString().isNullOrEmpty() || intent.getStringExtra(
                "email"
            ).toString().isNullOrEmpty()
        ) {
            startActivity(Intent(this, TabbedActivity::class.java))
            finish()
        } else {
            receiverId = intent.getStringExtra("receiverId").toString()
            email = intent.getStringExtra("email").toString()
            fetchMessages()
            textViewEmail.setText(email)
        }
        super.onStart()
    }

    override fun onBackPressed() {
        startActivity(Intent(this, TabbedActivity::class.java))
        finish()
        super.onBackPressed()
    }

    private fun playSound() {
        val mediaPlayer = MediaPlayer()
        val afd: AssetFileDescriptor = assets.openFd("hollow.mp3")
        mediaPlayer.setDataSource(afd.fileDescriptor)
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    private fun generateChannel(sender: String, receiver: String): String {
        return if (sender > receiver) {
            sender + "_" + receiver
        } else {
            receiver + "_" + sender
        }
    }

    private fun sendMessage() {
        if (receiverId.isNullOrEmpty()) {
            startActivity(Intent(this, TabbedActivity::class.java))
            finish()
        } else {
            val message = editTextMessage.text.toString()
            if (TextUtils.isEmpty(message)) {
                editTextMessage.requestFocus()
                editTextMessage.error = "Cannot be empty"
            } else {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val calendar = Calendar.getInstance().time

                database.child("my-chat").child("chats")
                    .child(
                        generateChannel(
                            auth.currentUser!!.uid,
                            receiverId
                        )
                    ).push()
                    .setValue(
                        MessageStructure(
                            auth.currentUser!!.uid,
                            receiverId,
                            message,
                            timeFormat.format(calendar),
                            dateFormat.format(calendar)
                        )
                    )
                    .addOnSuccessListener {
                        database.child("my-chat").child("display-chats")
                            .child(generateChannel(auth.currentUser!!.uid, receiverId))
                            .child("message").setValue(message)
                        database.child("my-chat").child("display-chats")
                            .child(generateChannel(auth.currentUser!!.uid, receiverId))
                            .child("date").setValue(dateFormat.format(calendar))
                        database.child("my-chat").child("display-chats")
                            .child(generateChannel(auth.currentUser!!.uid, receiverId))
                            .child(auth.currentUser!!.uid).setValue(auth.currentUser!!.email)
                        database.child("my-chat").child("display-chats")
                            .child(generateChannel(auth.currentUser!!.uid, receiverId))
                            .child(receiverId).setValue(email)
                        Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show()
                        recyclerViewMessages.scrollToPosition(recyclerViewMessages.adapter?.itemCount!!.toInt() - 1)
                        editTextMessage.text.clear()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Message not sent", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun fetchMessages() {
        val fetchChannelQuery = database.child("my-chat").child("chats")
        fetchChannelQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.e("Error", p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child(auth.currentUser!!.uid + "_" + receiverId).exists()) {
                    getMessages(auth.currentUser!!.uid + "_" + receiverId)
                } else if (p0.child(receiverId + "_" + auth.currentUser!!.uid)
                        .exists()
                ) {
                    getMessages(receiverId + "_" + auth.currentUser!!.uid)
                }
            }

        })
    }

    private fun getMessages(channel: String) {
        val fetchMessageQuery = database.child("my-chat").child("chats").child(channel)
        fetchMessageQuery.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.e("CHAT ERROR", p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                messagesList.clear()

                for (postSnapshot in p0.children) {
                    Log.e("CHAT DATA", postSnapshot.toString())
                    Log.e("CHAT DATA FILTERED", postSnapshot.toString())
                    val message = Message(
                        postSnapshot.child("senderId").value.toString(),
                        postSnapshot.child("time").value.toString(),
                        "",
                        postSnapshot.child("message").value.toString()
                    )
                    messagesList.add(message)
                }
                messagesAdapter = MessagesAdapter(messagesList as ArrayList<Message>)
                messagesAdapter.notifyDataSetChanged()
                recyclerViewMessages.adapter = messagesAdapter
                recyclerViewMessages.visibility = View.VISIBLE
                recyclerViewMessages.scrollToPosition(recyclerViewMessages.adapter?.itemCount!!.toInt() - 1)
                progressBarChats.visibility = View.GONE
                playSound()
            }

        })
    }

}