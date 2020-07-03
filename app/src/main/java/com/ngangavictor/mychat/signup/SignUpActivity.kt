package com.ngangavictor.mychat.signup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.ngangavictor.mychat.R

class SignUpActivity : AppCompatActivity() {

    private lateinit var editTextEmail:EditText
    private lateinit var editTextPass:EditText
    private lateinit var editTextCPass:EditText
    private lateinit var textViewForgot:TextView
    private lateinit var textViewLogin:TextView
    private lateinit var buttonRegister:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        editTextEmail=findViewById(R.id.editTextEmail)
        editTextPass=findViewById(R.id.editTextPass)
        editTextCPass=findViewById(R.id.editTextCPass)
        textViewForgot=findViewById(R.id.textViewForgot)
        textViewLogin=findViewById(R.id.textViewLogin)
        buttonRegister=findViewById(R.id.buttonRegister)
    }
}