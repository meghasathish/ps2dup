package com.example.ps2dup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeScreen: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        val presentation=findViewById<Button>(R.id.pres)
        presentation.setOnClickListener(){
            val intent= Intent(this,Timer::class.java)
            startActivity(intent)
            finish()
        }

    }
}