package com.ngangavictor.mychat

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ngangavictor.mychat.adapter.MessagesAdapter
import com.ngangavictor.mychat.adapter.RecipientAdapter
import com.ngangavictor.mychat.listeners.SelectedRecipient
import com.ngangavictor.mychat.models.Message
import com.ngangavictor.mychat.models.MessageStructure
import com.ngangavictor.mychat.models.Recipient
import com.ngangavictor.mychat.signin.SignInActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), SelectedRecipient {

    private lateinit var textViewReceiver: TextView
    private lateinit var editTextMessage: EditText
    private lateinit var imageButtonSend: ImageButton
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var dialog: AlertDialog
    private lateinit var database: DatabaseReference
    lateinit var messagesList: MutableList<Message>
    lateinit var messagesAdapter: MessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textViewReceiver = findViewById(R.id.textViewReceiver)
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages)
        floatingActionButton = findViewById(R.id.floatingActionButton)
        imageButtonSend = findViewById(R.id.imageButtonSend)
        editTextMessage = findViewById(R.id.editTextMessage)

        recyclerViewMessages.layoutManager = LinearLayoutManager(this)
        recyclerViewMessages.setHasFixedSize(true)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        messagesList = ArrayList()

        clickListeners()
    }

    private fun clickListeners() {
        floatingActionButton.setOnClickListener {
            chooseRecipient()
        }

        imageButtonSend.setOnClickListener { sendMessage() }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            fetchMessages()
        } else {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    private fun fetchMessages() {
        val fetchChannelQuery = database.child("my-chat").child("chats")
        fetchChannelQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child(auth.currentUser!!.uid + "_" + receiverId).exists()) {
                    getMessages(auth.currentUser!!.uid + "_" + receiverId)
                } else if (p0.child(receiverId + "_" + auth.currentUser!!.uid).exists()) {
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
            }

        })
    }

    private fun sendMessage() {
        if (receiverId == null) {
            chooseRecipient()
        } else {
            val message = editTextMessage.text.toString()
            if (TextUtils.isEmpty(message)) {
                editTextMessage.requestFocus()
                editTextMessage.error = "Cannot be empty"
            } else {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                val timeFormat = SimpleDateFormat("HH:mm:ss")
                val calendar = Calendar.getInstance().time

                database.child("my-chat").child("chats")
                    .child(generateChannel(auth.currentUser!!.uid, receiverId.toString())).push()
                    .setValue(
                        MessageStructure(
                            auth.currentUser!!.uid,
                            receiverId.toString(),
                            message,
                            timeFormat.format(calendar),
                            dateFormat.format(calendar)
                        )
                    )
                    .addOnSuccessListener {
                        Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show()
                        editTextMessage.text.clear()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Message not sent", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun generateChannel(sender: String, receiver: String): String {
        return if (sender > receiver) {
            sender + "_" + receiver
        } else {
            receiver + "_" + sender
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun chooseRecipient() {
        val getUsersQuery = database.child("my-chat").child("users")
        val recipientList: MutableList<Recipient> = ArrayList()
        var adapter: RecipientAdapter? = null
        val alert = AlertDialog.Builder(this)
        alert.setCancelable(false)
        alert.setTitle("Choose Recipient")
        val recyclerView = RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        getUsersQuery.addValueEventListener(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                recipientList.clear()
                for (postSnapshot in p0.children) {
                    if (postSnapshot.key != auth.currentUser?.uid) {
                        val recipient = Recipient(
                            postSnapshot.child("email").value.toString(),
                            postSnapshot.key.toString()
                        )
                        recipientList.add(recipient)
                    }
                }
                adapter = RecipientAdapter(
                    recipientList as ArrayList<Recipient>, this@MainActivity
                )
                adapter?.notifyDataSetChanged()
                recyclerView.adapter = adapter
            }
        })
        alert.setView(recyclerView)
        alert.setNegativeButton(
            "Cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        dialog = alert.create()
        recyclerView.setOnClickListener {
            dialog.cancel()
        }
        dialog.show()
    }

    override fun setEmail(username: String) {
        textViewReceiver.text = "You are chatting with " + username
        dialog.cancel()
        fetchMessages()
    }

    override fun setRecipientId(recipientId: String) {
        receiverId = recipientId
    }

    companion object {
        var receiverId: String? = null
    }

}