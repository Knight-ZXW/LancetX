package com.knightboost.lancetx

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ImplA().testMethod()
        ClassA().printMessage("haha!")

        init_method_insert_test.setOnClickListener {
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