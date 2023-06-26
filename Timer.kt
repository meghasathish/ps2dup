package com.example.ps2dup

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

open class Timer:AppCompatActivity() {
    public var countdowntimer: CountDownTimer? = null
    public var time_in_milliseconds = 15000L
    public var pauseOffSet = 0L
//    private var timeSelected: Int = 0
//    lateinit var sec: TextView
//    private var i: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)
        val next = findViewById<Button>(R.id.nextbtn)
        next.setOnClickListener() {
            val intent = Intent(this, Camera::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun starTimer(pauseOffSetL : Long){
        val tv_timer=findViewById<TextView>(R.id.tv_timer)
        countdowntimer = object : CountDownTimer(time_in_milliseconds - pauseOffSetL, 1000){
            override fun onTick(millisUntilFinished: Long) {
                pauseOffSet = time_in_milliseconds - millisUntilFinished
                tv_timer.text= (millisUntilFinished/1000).toString()
            }
            override fun onFinish() {
                Toast.makeText(this@Timer, "Timer finished", Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun pauseTimer(){
        if (countdowntimer!= null){
            countdowntimer!!.cancel()
        }
    }

    private fun resetTimer(){
        val tv_timer=findViewById<TextView>(R.id.tv_timer)
        if (countdowntimer!= null){
            countdowntimer!!.cancel()
            tv_timer.text = (time_in_milliseconds/1000).toString()
            countdowntimer = null
            pauseOffSet =0
        }
    }

}

//        val inc=findViewById<ImageView>(R.id.inc)
//
//        inc.setOnClickListener(){
//
//            incTime()
//        }

//    }
//    fun incTime() {
//        var sec=findViewById<TextView>(R.id.seconds)
//        if(timeSelected!=0){
//            timeSelected+=10
//            sec.text=timeSelected.toString()
//
//            //Toast.makeText(this, "button clicked", Toast.LENGTH_SHORT).show()
//        }
//
//
//
//
//    }
//}
