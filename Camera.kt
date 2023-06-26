package com.example.ps2dup

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

@Suppress("DEPRECATION")
open class Camera : Timer() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        val startBtn=findViewById<Button>(R.id.begin)
        val img= arrayOf(R.drawable.mic)
        startBtn.isEnabled=false
        val cam = findViewById<ImageView>(R.id.camera)
        cam.setOnClickListener() {
            get_permission()
            cam.setImageResource(img[0])
            startBtn.isEnabled=true
            startBtn.setBackgroundColor(ContextCompat.getColor(this,R.color.blue))
            startBtn.setOnClickListener(){
                val intent= Intent(this,VideoCapture::class.java)
                startActivity(intent)
                finish()
            }
        }
        val timerTxt= findViewById<TextView>(R.id.tvTimer)
        timerTxt.text= (time_in_milliseconds/1000).toString()
    }

    fun get_permission() {
        var permission = mutableListOf<String>()
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) permission.add(
            android.Manifest.permission.CAMERA
        )
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) permission.add(
            android.Manifest.permission.RECORD_AUDIO
        )
        if(permission.size>0){
            requestPermissions(permission.toTypedArray(),101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        grantResults.forEach {
            if(it!= PackageManager.PERMISSION_GRANTED){
                get_permission()
            }
        }
    }

}