package com.ngangavictor.mychat.signin

import android.content.DialogInterface
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
import com.ngangavictor.mychat.R
import com.ngangavictor.mychat.main.TabbedActivity
import com.ngangavictor.mychat.signup.SignUpActivity

class SignInActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPass: EditText
    private lateinit var textViewForgot: TextView
    private lateinit var textViewRegister: TextView
    private lateinit var buttonLogin: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var alert: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPass = findViewById(R.id.editTextPass)
        textViewForgot = findViewById(R.id.textViewForgot)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewRegister = findViewById(R.id.textViewRegister)

        auth = FirebaseAuth.getInstance()

        clickListeners()
    }

    private fun clickListeners() {
        textViewRegister.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        textViewForgot.setOnClickListener { }

        buttonLogin.setOnClickListener { login() }
    }

    private fun login() {
        val email = editTextEmail.text.toString()
        val password = editTextPass.text.toString()
        if (TextUtils.isEmpty(email)) {
            editTextEmail.error = "Cannot be empty"
            editTextEmail.requestFocus()
        } else if (TextUtils.isEmpty(password)) {
            editTextPass.error = "Cannot be empty"
            editTextPass.requestFocus()
        } else {
            alertProgress()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        alertDialog("Success", "You have successfully signed in")
                    }
                }.addOnFailureListener {
                    alertDialog("Error", it.message.toString())
                }
        }
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
            alertDialog.setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                auth.signOut()
                alert.cancel()
            })
        }

        alertDialog.setView(customLayout)
        alertDialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
            if (title == "Success") {
                startActivity(Intent(this, TabbedActivity::class.java))
                finish()
            } else if (title == "Error") {
                alert.cancel()
            } else if (title == "Confirmation") {
                startActivity(Intent(this, TabbedActivity::class.java))
                finish()
            }
        })
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