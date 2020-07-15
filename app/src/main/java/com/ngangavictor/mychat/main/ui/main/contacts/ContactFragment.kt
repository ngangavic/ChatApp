package com.ngangavictor.mychat.main.ui.main.contacts

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ngangavictor.mychat.R
import com.ngangavictor.mychat.adapter.ContactSearchAdapter
import com.ngangavictor.mychat.adapter.ContactsAdapter
import com.ngangavictor.mychat.adapter.DisplayMessagesAdapter
import com.ngangavictor.mychat.listeners.SelectedContact
import com.ngangavictor.mychat.models.Contact
import com.ngangavictor.mychat.models.ContactSearch
import com.ngangavictor.mychat.models.DisplayMessages

class ContactFragment : Fragment(), SelectedContact {

    private lateinit var floatingActionButtonAdd: FloatingActionButton
    private lateinit var recyclerViewContacts: RecyclerView
    lateinit var contactsList: MutableList<Contact>
    lateinit var contactsAdapter: ContactsAdapter
    private lateinit var root: View
    private lateinit var alertDialog: AlertDialog
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_contact, container, false)
        floatingActionButtonAdd = root.findViewById(R.id.floatingActionButtonAdd)
        recyclerViewContacts = root.findViewById(R.id.recyclerViewContacts)

        recyclerViewContacts.layoutManager = LinearLayoutManager(context)
        recyclerViewContacts.setHasFixedSize(true)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        floatingActionButtonAdd.setOnClickListener { searchContactAlert() }

        contactsList = ArrayList()

        loadContacts()

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

        alertSearch.setNegativeButton(
            "Cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        buttonSearch.setOnClickListener {
            contactSearchList.clear()
            val getUsersQuery = database.child("my-chat").child("users")
            getUsersQuery.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        if (data.key != auth.currentUser?.uid) {
                            val email = data.child("email").value.toString()
                            if (email.contains(editTextSearch.text.toString())) {
                                val contactSearch =
                                    ContactSearch(data.child("email").value.toString())
                                contactSearchList.add(contactSearch)
                            }
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

    private fun addContact(email: String) {
        val checkContactQuery =
            database.child("my-chat").child("contacts").child(auth.currentUser!!.uid)
                .child(emailTrim(email))
        checkContactQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    alertDialog.cancel()
                    Snackbar.make(requireView(), "Contact exist", Snackbar.LENGTH_SHORT).show()
                } else {

                    database.child("my-chat").child("contacts")
                        .child(auth.currentUser!!.uid).child(emailTrim(email)).setValue(email)
                        .addOnSuccessListener {
                            alertDialog.cancel()
                            Snackbar.make(requireView(), "Contact added", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                        .addOnFailureListener {
                            alertDialog.cancel()
                            Snackbar.make(
                                requireView(),
                                "Contact add error",
                                Snackbar.LENGTH_SHORT
                            )
                                .show()
                        }

                }
            }

        })

    }

    private fun emailTrim(email: String): String {
        return email.replace(".", "").replace("@", "")
    }

    private fun loadContacts() {
        val getContactsQuery =
            database.child("my-chat").child("contacts").child(auth.currentUser!!.uid)

        getContactsQuery.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                error.message
            }

            override fun onDataChange(snapshot: DataSnapshot) {
               for (data in snapshot.children){
                   val contact=Contact(data.value.toString())
                   Log.e("CONTACT VALUE",data.value.toString())
                   contactsList.add(contact)
               }
                contactsAdapter=ContactsAdapter(contactsList as ArrayList<Contact>)
                contactsAdapter.notifyDataSetChanged()
                recyclerViewContacts.adapter = contactsAdapter
                recyclerViewContacts.visibility = View.VISIBLE
            }

        })
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
        addContact(email)
    }
}