package com.ngangavictor.mychat.main.ui.main.contacts

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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
import com.ngangavictor.mychat.adapter.ContactSearchAdapter
import com.ngangavictor.mychat.adapter.RecipientAdapter
import com.ngangavictor.mychat.listeners.SelectedContact
import com.ngangavictor.mychat.models.ContactSearch
import com.ngangavictor.mychat.models.Recipient

class ContactFragment : Fragment(), SelectedContact {

    private lateinit var floatingActionButtonAdd: FloatingActionButton
    private lateinit var root: View
    private lateinit var alertDialog: AlertDialog
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_contact, container, false)
        floatingActionButtonAdd = root.findViewById(R.id.floatingActionButtonAdd)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        floatingActionButtonAdd.setOnClickListener { searchContactAlert() }

        return root
    }

    private fun searchContactAlert() {
        val contactSearchList: MutableList<ContactSearch> = ArrayList()
        var adapter: ContactSearchAdapter
        val alertSearch = AlertDialog.Builder(requireContext())
        alertSearch.setCancelable(false)
        val layout = layoutInflater.inflate(R.layout.search_contact_alert, null)
        alertSearch.setView(layout)
        val recyclerView = layout.findViewById<RecyclerView>(R.id.recyclerViewContactSearch)
        val editTextSearch = layout.findViewById<EditText>(R.id.editTextTextEmailAddress)
        val buttonSearch = layout.findViewById<ImageButton>(R.id.imageButtonSearch)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.setHasFixedSize(true)

        alertSearch.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->dialog.cancel()  })

        buttonSearch.setOnClickListener {
            contactSearchList.clear()
            val getUsersQuery = database.child("my-chat").child("users")
            getUsersQuery.addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children){
                        val email=data.child("email").value.toString()
                        if (email.contains(editTextSearch.text.toString())){
                            val contactSearch=ContactSearch(data.child("email").value.toString())
                            contactSearchList.add(contactSearch)
                        }
                    }
                    adapter = ContactSearchAdapter(
                        contactSearchList as ArrayList<ContactSearch>, this@ContactFragment
                    )
                    adapter.notifyDataSetChanged()
                    recyclerView.adapter = adapter
                }

            })
         }

        alertDialog = alertSearch.create()
        alertDialog.show()
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ContactFragment().apply {
                arguments = Bundle().apply {
                    putString("ARG_PARAM1", param1)
                    putString("ARG_PARAM2", param2)
                }
            }
    }

    override fun chosenEmail(email: String) {

    }
}