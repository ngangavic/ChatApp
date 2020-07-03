package com.ngangavictor.mychat

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
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
import com.ngangavictor.mychat.adapter.RecipientAdapter
import com.ngangavictor.mychat.listeners.SelectedRecipient
import com.ngangavictor.mychat.models.Recipient
import com.ngangavictor.mychat.signin.SignInActivity

class MainActivity : AppCompatActivity(), SelectedRecipient {

    private lateinit var textViewReceiver: TextView
    private lateinit var editTextMessage: EditText
    private lateinit var imageButtonSend: ImageButton
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var dialog: AlertDialog
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textViewReceiver = findViewById(R.id.textViewReceiver)
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages)
        floatingActionButton = findViewById(R.id.floatingActionButton)
        imageButtonSend = findViewById(R.id.imageButtonSend)
        editTextMessage = findViewById(R.id.editTextMessage)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        clickListeners()
    }

    private fun clickListeners() {
        floatingActionButton.setOnClickListener {
            chooseRecipient()
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
                startActivity(Intent(this,SignInActivity::class.java))
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
//        fetchMessages()
    }

    override fun setRecipientId(recipientId: String) {
        receiverId = recipientId
    }

    companion object {
        var receiverId: String? = null
    }

}