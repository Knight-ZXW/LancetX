package com.knightboost.lancetx.weaver;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import com.knightboost.weaver.api.Origin;
import com.knightboost.weaver.api.Scope;
import com.knightboost.weaver.api.This;
import com.knightboost.weaver.api.Weaver;
import com.knightboost.weaver.api.WeaverJoinPoint;
import com.knightboost.weaver.api.annotations.Group;
import com.knightboost.weaver.api.annotations.Insert;
import com.knightboost.weaver.api.annotations.TargetClass;
import com.knightboost.weaver.api.annotations.Weave;


@Weave
@Group("apmActivityWeaver")
public class ActivityMethodWeaver extends Weaver {
    private static final String BASE_ACTIVITY = "android.app.Activity";

    @Keep
    @Insert(value = "onCreate",mayCreateSuper = true)
    @TargetClass(value = BASE_ACTIVITY,scope = Scope.ALL_CHILDREN)
    public void onCreate(@Nullable Bundle savedInstanceState){
        long begin = System.currentTimeMillis();
        String instanceClassName = This.get().getClass().getCanonicalName();
        String declaredName = WeaverJoinPoint.ClassNameOfJoinPoint;
        Origin.callVoid();
        if (!instanceClassName.equals(declaredName)){
            return;
        }
        Object curO = This.get();
        Log.e("zxw",curO+" onCreate");
        long end = System.currentTimeMillis();
    }
    @Keep
    @Insert(value = "onStart",mayCreateSuper = true)
    @TargetClass(value = BASE_ACTIVITY,scope = Scope.ALL_CHILDREN)
    protected void onStart(){
        long begin = System.currentTimeMillis();
        Origin.callVoid();
        Object curO = This.get();
        Log.e("zxw",curO+" onStart");
        long end = System.currentTimeMillis();
    }

    @Keep
    @Insert(value = "onResume",mayCreateSuper = true)
    @TargetClass(value = BASE_ACTIVITY,scope = Scope.ALL_CHILDREN)
    protected void onResume(){
        long begin = System.currentTimeMillis();
        Origin.callVoid();
        Object curO = This.get();
        Log.e("zxw",curO+" onResume");
        long end = System.currentTimeMillis();
    }

    @Keep
    @Insert(value = "onPause",mayCreateSuper = true)
    @TargetClass(value = BASE_ACTIVITY,scope = Scope.ALL_CHILDREN)
    protected void onPause(){
        long begin = System.currentTimeMillis();
        Origin.callVoid();
        long end = System.currentTimeMillis();

    }



    @Keep
    @Insert(value = "onStop",mayCreateSuper = true)
    @TargetClass(value = BASE_ACTIVITY,scope = Scope.LEAF)
    protected void onStop(){
        long begin = System.currentTimeMillis();
        Origin.callVoid();
        long end = System.currentTimeMillis();
        long cost = end -begin;
        Activity activity = (Activity) This.get();
    }

    @Insert(value = "onDestroy",mayCreateSuper = true)
    @TargetClass(value = BASE_ACTIVITY,scope = Scope.LEAF)
    @Keep
    protected void onDestroy(){
        long begin = System.currentTimeMillis();
        Origin.callVoid();
        long end = System.currentTimeMillis();
        long cost = end -begin;
        Activity activity = (Activity) This.get();
    }
}
