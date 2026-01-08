package com.knightboost.lancetx

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ImplA().testMethod()
        ClassA().printMessage("haha!")
        findViewById<View>(R.id.init_method_insert_test)
            .setOnClickListener {
            ConstructorTest(" original call^");
        }
    }

    override fun onResume() {
        super.onResume()
        val thread = Thread()
        val intent = Intent()
        Log.i("Activity","onResume")
    }

    fun normalMethod(){
        Log.i("Activity","normalMethod")
        val thread = Thread()
    }

}