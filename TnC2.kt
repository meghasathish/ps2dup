package com.example.ps2dup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class TnC2:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tnc)

        val pvc = findViewById<CheckBox>(R.id.checkBoxpvc)
        val proceed = findViewById<Button>(R.id.proceed)

        pvc.setOnCheckedChangeListener{ buttonView, isChecked ->
            if(buttonView.isChecked){
                proceed.isEnabled=true
                proceed.setBackgroundColor(ContextCompat.getColor(this,R.color.blue))
                proceed.setOnClickListener(){
                    val intent=Intent(this, Login::class.java)
                    startActivity(intent)
                    finish()
                }
            }else{
                proceed.isEnabled=false
            }
            //Toast.makeText(this,isChecked.toString(), Toast.LENGTH_SHORT).show()
        }


    }
}