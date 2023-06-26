package com.example.ps2dup


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity

class LoginSignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginsignup)

        val sign = findViewById<Button>(R.id.signin)
        sign.setOnClickListener {
            intentActivity2()
        }
        val acc = findViewById<Button>(R.id.createAcc)
        acc.setOnClickListener {
            intentActivity()
        }
    }

    private fun intentActivity() {
        val intent = Intent(this, TnC::class.java)
        startActivity(intent)
        finish()
    }

    private fun intentActivity2() {
        val intent = Intent(this, TnC2::class.java)
        startActivity(intent)
        finish()
    }
}