package com.knightboost.lancetx

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ImplA().testMethod()

        init_method_insert_test.setOnClickListener {
            ConstructorTest(" original call^");
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i("Activity","onResume")
    }
}