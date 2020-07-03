package com.ngangavictor.mychat.signin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.ngangavictor.mychat.R

class SignInActivity : AppCompatActivity() {

    private lateinit var editTextEmail:EditText
    private lateinit var editTextPass:EditText
    private lateinit var textViewForgot:TextView
    private lateinit var textViewRegister:TextView
    private lateinit var buttonLogin:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        editTextEmail=findViewById(R.id.editTextEmail)
        editTextPass=findViewById(R.id.editTextPass)
        textViewForgot=findViewById(R.id.textViewForgot)
        buttonLogin=findViewById(R.id.buttonLogin)
        textViewRegister=findViewById(R.id.textViewRegister)
    }
}