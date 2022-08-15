package com.knightboost.lancetx.weaver;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.knightboost.lancet.api.Origin;
import com.knightboost.lancet.api.Scope;
import com.knightboost.lancet.api.This;
import com.knightboost.lancet.api.annotations.Insert;
import com.knightboost.lancet.api.annotations.TargetClass;
import com.knightboost.lancet.api.annotations.TargetMethod;
import com.knightboost.lancet.api.annotations.Weaver;

@Weaver
public class ConstructorInsert {
    //<init>
    // @Insert(mayCreateSuper = false)
    // @TargetMethod(methodName = "<init>")
    // @TargetClass(value = "com.knightboost.lancetx.ConstructorTest",
    //         scope = Scope.SELF)
    // public void wrapConstructorInit(@Nullable String savedInstanceState) {
    //     Log.e("ConstructorInsertTest","call before original instructor invoked");
    //     Origin.callVoid();
    //     Log.e("ConstructorInsertTest","call after original instructor invoked");
    // }
}
