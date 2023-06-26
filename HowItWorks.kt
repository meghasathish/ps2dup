package com.example.ps2dup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HowItWorks: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_howitworks)
        val nextbutton=findViewById<Button>(R.id.next)
        nextbutton.setOnClickListener(){
            nextbtn()
        }
        val skipbutton=findViewById<Button>(R.id.skip)
        skipbutton.setOnClickListener(){
            intent_activity()
        }
    }

    val labels = arrayOf("Speaking Analysis","Voice Analysis","Body Analysis","Facial Expression")
    val img= arrayOf(R.drawable.speaking_analysis,R.drawable.voice_analysis,R.drawable.body_analysis,R.drawable.facial_expression)
    var currentView=0
    public fun nextbtn(){
        val textMsg = findViewById<TextView>(R.id.speakingAnalysis)
        val image: ImageView = findViewById<View>(R.id.mic) as ImageView
        currentView=currentView+1
        if(currentView>labels.size-1){
            intent_activity()
        }
        textMsg.text=labels[currentView]
        image.setImageResource(img[currentView])
    }

    public fun intent_activity(){
        val intent= Intent(this,LoginSignUp::class.java)
        startActivity(intent)
        finish()
    }

}