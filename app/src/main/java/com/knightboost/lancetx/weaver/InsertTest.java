package com.knightboost.lancetx.weaver;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import com.knightboost.lancet.api.Origin;
import com.knightboost.lancet.api.Scope;
import com.knightboost.lancet.api.This;
import com.knightboost.lancet.api.WeaverJoinPoint;
import com.knightboost.lancet.api.annotations.Group;
import com.knightboost.lancet.api.annotations.Insert;
import com.knightboost.lancet.api.annotations.TargetClass;
import com.knightboost.lancet.api.annotations.TargetMethod;
import com.knightboost.lancet.api.annotations.Weaver;

@Weaver
@Group("insertTest")
public class InsertTest {
    private static final String BASE_ACTIVITY = "android.app.Activity";

    @Keep
    @Insert(mayCreateSuper = true)
    @TargetMethod(methodName = "onCreate")
    @TargetClass(value = "android.app.Activity", scope = Scope.LEAF)
    public void onCreate(@Nullable Bundle savedInstanceState) {
        long begin = System.currentTimeMillis();
        String instanceClassName = This.get().getClass().getCanonicalName();
        String declaredName = WeaverJoinPoint.ClassNameOfJoinPoint;
        Origin.callVoid();
        if (!instanceClassName.equals(declaredName)) {
            return;
        }
        Object curO = This.get();
        Log.e("insertTest", curO + " onCreate");
        long end = System.currentTimeMillis();
    }

    @Keep
    @Insert(mayCreateSuper = true)
    @TargetMethod(methodName = "onCreate")
    @TargetClass(value = "android.app.Activity", scope = Scope.LEAF)
    public void onCreate2(@Nullable Bundle savedInstanceState) {
        long begin = System.currentTimeMillis();
        Origin.callVoid();
        long end = System.currentTimeMillis();
        Activity activity = ((Activity) This.get());
        Log.e("insertTest", activity + " onCreate cost "+(end-begin)+" ms");
    }



    @Keep
    @TargetMethod(methodName = "onStart")
    @TargetClass(value = BASE_ACTIVITY, scope = Scope.LEAF)
    @Insert(mayCreateSuper = true)
    protected void onStart() {
        long begin = System.currentTimeMillis();
        Origin.callVoid();
        Object curO = This.get();
        Log.e("insertTest", curO + " onStart");
        long end = System.currentTimeMillis();
    }

    @Keep
    @Insert(mayCreateSuper = true)
    @TargetClass(value = BASE_ACTIVITY, scope = Scope.LEAF)
    @TargetMethod(methodName = "onResume")
    protected void onResume() {
        long begin = System.currentTimeMillis();
        Origin.callVoid();
        Object curO = This.get();
        Log.e("insertTest", curO + " onResume");
        long end = System.currentTimeMillis();
    }

    @Keep
    @Insert(mayCreateSuper = true)
    @TargetClass(value = BASE_ACTIVITY, scope = Scope.LEAF)
    @TargetMethod(methodName = "onPause")
    protected void onPause() {
        long begin = System.currentTimeMillis();
        Origin.callVoid();
        long end = System.currentTimeMillis();

    }

    @Keep
    @Insert(mayCreateSuper = true)
    @TargetClass(value = BASE_ACTIVITY, scope = Scope.LEAF)
    @TargetMethod(methodName = "onStop")
    protected void onStop() {
        long begin = System.currentTimeMillis();
        Origin.callVoid();
        long end = System.currentTimeMillis();
        long cost = end - begin;
        Activity activity = (Activity) This.get();
    }

    @TargetClass(value = BASE_ACTIVITY, scope = Scope.LEAF)
    @TargetMethod(methodName = "onDestroy")
    @Keep
    protected void onDestroy() {
        long begin = System.currentTimeMillis();
        Origin.callVoid();
        long end = System.currentTimeMillis();
        long cost = end - begin;
        Activity activity = (Activity) This.get();
    }
}
