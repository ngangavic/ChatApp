package com.ngangavictor.mychat.main.ui.main.chats

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
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
import com.ngangavictor.mychat.adapter.DisplayMessagesAdapter
import com.ngangavictor.mychat.chat.ChatActivity
import com.ngangavictor.mychat.listeners.SelectedRecipient
import com.ngangavictor.mychat.models.DisplayMessages

class ChatFragment : Fragment(), SelectedRecipient {

    private lateinit var progressBarChats: ProgressBar
    private lateinit var recyclerViewChats: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    lateinit var displayMessagesList: MutableList<DisplayMessages>
    lateinit var displayMessagesAdapter: DisplayMessagesAdapter
    private lateinit var root: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_chat, container, false)

        recyclerViewChats = root.findViewById(R.id.recyclerViewChats)
        progressBarChats = root.findViewById(R.id.progressBarChats)

        recyclerViewChats.layoutManager = LinearLayoutManager(context)
        recyclerViewChats.setHasFixedSize(true)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        displayMessagesList = ArrayList()

        fetchDisplayMessages()
        return root
    }

    private fun fetchDisplayMessages() {
        val fetchDisplayMessagesQuery = database.child("my-chat").child("display-chats")
        fetchDisplayMessagesQuery.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                displayMessagesList.clear()
                for (data in snapshot.children) {
                    data.key
                    Log.e("LAST KEY", data.key.toString().takeLast(28))
                    Log.e("FIRST KEY", data.key.toString().take(28))
                    if (auth.currentUser!!.uid == data.key.toString()
                            .takeLast(28) || auth.currentUser!!.uid == data.key.toString()
                            .take(28)
                    ) {
                        val displayMessage = DisplayMessages(
                            data.child("message").value.toString(),
                            data.child(getOtherUid(data.key.toString())).value.toString(),
                            getOtherUid(data.key.toString()),
                            data.child("date").value.toString()
                        )
                        displayMessagesList.add(displayMessage)
                    }
                }
                if (displayMessagesList.size == 0) {
                    progressBarChats.visibility = View.GONE
                } else {
                    displayMessagesAdapter =
                        DisplayMessagesAdapter(
                            displayMessagesList as ArrayList<DisplayMessages>,
                            this@ChatFragment
                        )
                    displayMessagesAdapter.notifyDataSetChanged()
                    recyclerViewChats.adapter = displayMessagesAdapter
                    progressBarChats.visibility = View.GONE
                    recyclerViewChats.visibility = View.VISIBLE
                }
            }
        })
    }


    private fun getOtherUid(key: String): String {
        return if (key.take(28) != auth.currentUser!!.uid) {
            key.take(28)
        } else {
            key.takeLast(28)
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString("ARG_PARAM1", param1)
                    putString("ARG_PARAM2", param2)
                }
            }
    }

    override fun setRecipientDetails(email: String, recipientId: String) {
        requireActivity().startActivity(
            Intent(
                requireActivity(),
                ChatActivity::class.java
            ).putExtra("email", email).putExtra("receiverId", recipientId)
        )
        requireActivity().finish()
    }
}