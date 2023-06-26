package com.example.ps2dup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat

class TnC:AppCompatActivity() {
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
                    val intent= Intent(this,SignUp::class.java)
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