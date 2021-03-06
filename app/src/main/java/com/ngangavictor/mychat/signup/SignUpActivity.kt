package com.ngangavictor.mychat.signup

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ngangavictor.mychat.R
import com.ngangavictor.mychat.main.TabbedActivity
import com.ngangavictor.mychat.signin.SignInActivity

class SignUpActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPass: EditText
    private lateinit var editTextCPass: EditText
    private lateinit var textViewForgot: TextView
    private lateinit var textViewLogin: TextView
    private lateinit var buttonRegister: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var alert: AlertDialog
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPass = findViewById(R.id.editTextPass)
        editTextCPass = findViewById(R.id.editTextCPass)
        textViewForgot = findViewById(R.id.textViewForgot)
        textViewLogin = findViewById(R.id.textViewLogin)
        buttonRegister = findViewById(R.id.buttonRegister)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        clickListeners()
    }

    private fun clickListeners() {
        textViewLogin.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        textViewForgot.setOnClickListener { }

        buttonRegister.setOnClickListener { register() }
    }

    private fun register() {
        val email = editTextEmail.text.toString()
        val pass = editTextPass.text.toString()
        val cPass = editTextCPass.text.toString()

        if (TextUtils.isEmpty(email)) {
            editTextEmail.requestFocus()
            editTextEmail.error = "Cannot be empty"

        } else if (TextUtils.isEmpty(pass)) {
            editTextPass.requestFocus()
            editTextPass.error = "Cannot be empty"
        } else if (TextUtils.isEmpty(cPass)) {
            editTextCPass.requestFocus()
            editTextCPass.error = "Cannot be empty"
        } else if (!checkEmail(email)) {
            editTextEmail.requestFocus()
            editTextEmail.error = "Invalid email"
        } else if (!checkPasswordLength(pass)) {
            editTextPass.requestFocus()
            editTextPass.error = "Password too short"
        } else if (!checkPasswordMatch(pass, cPass)) {
            editTextCPass.requestFocus()
            editTextCPass.error = "Passwords do not match"
        } else {
            alertProgress()
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        database.child("my-chat").child("users").child(auth.currentUser!!.uid)
                            .child("email")
                            .setValue(email)
                            .addOnSuccessListener {
                                alert.cancel()
                                alertDialog(
                                    "Success",
                                    "Registration was successful. Enjoy using My Chat app"
                                )
                            }
                            .addOnFailureListener {
                                alert.cancel()
                                alertDialog("Error", it.message.toString())
                            }
                    }
                }.addOnFailureListener {
                    alert.cancel()
                    alertDialog("Error", it.message.toString())
                }
        }

    }

    private fun checkPasswordLength(value: String): Boolean {
        return value.length > 6
    }

    private fun checkPasswordMatch(p1: String, p2: String): Boolean {
        return p1 == p2
    }

    private fun checkEmail(value: String): Boolean {
        return value.contains("@") && value.contains(".")
    }

    private fun alertProgress() {
        val alertDialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        alertDialog.setCancelable(false)
        val progressBar = ProgressBar(this)
        alertDialog.setView(progressBar)
        alert = alertDialog.create()
        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alert.show()
    }

    private fun alertDialog(title: String, message: String) {
        val alertDialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        alertDialog.setCancelable(false)
        val customLayout = layoutInflater.inflate(R.layout.signup_alert, null)
        val textViewTitle: TextView = customLayout.findViewById(R.id.textViewTitle)
        val textViewMessage: TextView = customLayout.findViewById(R.id.textViewMessage)
        textViewTitle.text = title
        textViewMessage.text = message

        if (title == "Confirmation") {
            alertDialog.setNegativeButton("No") { _, _ ->
                auth.signOut()
                alert.cancel()
            }
        }

        alertDialog.setView(customLayout)
        alertDialog.setPositiveButton("Ok") { _, _ ->
            when (title) {
                "Success" -> {
                    startActivity(Intent(this, TabbedActivity::class.java))
                    finish()
                }
                "Error" -> {
                    alert.cancel()
                }
                "Confirmation" -> {
                    startActivity(Intent(this, TabbedActivity::class.java))
                    finish()
                }
            }
        }
        alert = alertDialog.create()
        alert.show()

    }

    override fun onStart() {
        super.onStart()
        alertProgress()
        if (auth.currentUser != null) {
            alert.cancel()
            alertDialog("Confirmation", "Do you want to continue as " + auth.currentUser!!.email)
        } else {
            alert.cancel()
        }
    }
}