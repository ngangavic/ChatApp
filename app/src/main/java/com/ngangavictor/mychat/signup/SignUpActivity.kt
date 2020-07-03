package com.ngangavictor.mychat.signup

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
import com.ngangavictor.mychat.MainActivity
import com.ngangavictor.mychat.R
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
                        val user = auth.currentUser
                        alert.cancel()
                        alertDialog(
                            "Success",
                            "Registration was successful. Enjoy using My Chat app"
                        )

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

        alertDialog.setView(customLayout)
        alertDialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
            if (title == "Success") {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else if (title == "Error") {
                alert.cancel()
            }
        })
        alert = alertDialog.create()
        alert.show()

    }

}