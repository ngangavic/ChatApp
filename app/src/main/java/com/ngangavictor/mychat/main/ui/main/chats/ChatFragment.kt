package com.ngangavictor.mychat.main.ui.main.chats

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
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
import com.ngangavictor.mychat.R
import com.ngangavictor.mychat.adapter.MessagesAdapter
import com.ngangavictor.mychat.adapter.RecipientAdapter
import com.ngangavictor.mychat.listeners.SelectedRecipient
import com.ngangavictor.mychat.models.Message
import com.ngangavictor.mychat.models.MessageStructure
import com.ngangavictor.mychat.models.Recipient
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatFragment : Fragment(), SelectedRecipient {

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
    lateinit var root: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_chat, container, false)

        textViewReceiver = root.findViewById(R.id.textViewReceiver)
        recyclerViewMessages = root.findViewById(R.id.recyclerViewMessages)
        floatingActionButton = root.findViewById(R.id.floatingActionButton)
        imageButtonSend = root.findViewById(R.id.imageButtonSend)
        editTextMessage = root.findViewById(R.id.editTextMessage)

        recyclerViewMessages.layoutManager = LinearLayoutManager(context)
        recyclerViewMessages.setHasFixedSize(true)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        messagesList = ArrayList()

        clickListeners()
        return root
    }

    private fun clickListeners() {
        floatingActionButton.setOnClickListener {
            chooseRecipient()
        }

        imageButtonSend.setOnClickListener { sendMessage() }
    }

    private fun playSound() {
        val mediaPlayer = MediaPlayer()
        val afd: AssetFileDescriptor = context!!.assets.openFd("hollow.mp3")
        mediaPlayer.setDataSource(afd.fileDescriptor)
        mediaPlayer.prepare()
        mediaPlayer.start()
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
                playSound()
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
                    .child(
                        generateChannel(
                            auth.currentUser!!.uid,
                            receiverId.toString()
                        )
                    ).push()
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
                        Toast.makeText(context, "Message sent", Toast.LENGTH_SHORT).show()
                        recyclerViewMessages.scrollToPosition(recyclerViewMessages.adapter?.itemCount!!.toInt() - 1)
                        editTextMessage.text.clear()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Message not sent", Toast.LENGTH_SHORT).show()
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

    private fun chooseRecipient() {
        val getUsersQuery = database.child("my-chat").child("users")
        val recipientList: MutableList<Recipient> = ArrayList()
        var adapter: RecipientAdapter
        val alert = AlertDialog.Builder(requireActivity())
        alert.setCancelable(false)
        alert.setTitle("Choose Recipient")
        val recyclerView = RecyclerView(requireActivity())
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
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
                    recipientList as ArrayList<Recipient>, this@ChatFragment
                )
                adapter.notifyDataSetChanged()
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

    @SuppressLint("SetTextI18n")
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

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString("ARG_PARAM1", param1)
                    putString("ARG_PARAM2", param2)
                }
            }
    }
}