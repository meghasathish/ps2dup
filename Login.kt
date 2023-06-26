package com.example.ps2dup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class Login: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginBtn=findViewById<Button>(R.id.log)
        val createAcc=findViewById<Button>(R.id.createacc)

        loginBtn.setOnClickListener(){
            val intent= Intent(this,WelcomeScreen::class.java)
            startActivity(intent)
            finish()
        }
        createAcc.setOnClickListener(){
            val intent= Intent(this,SignUp::class.java)
            startActivity(intent)
            finish()
        }
    }

}