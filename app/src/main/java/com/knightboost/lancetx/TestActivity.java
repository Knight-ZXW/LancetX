package com.knightboost.lancetx;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("zxw","original OnCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread().start();
    }

    @AppSpeed(section = "xx")
    public void t(){

    }
}
