package com.knightboost.lancetx

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.knightboost.lancetx.weaver.TO

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val t = TO()
        t.test(null,null,null)
    }

    override fun onResume() {
        super.onResume()
        Log.i("Activity","onResume")
    }
}