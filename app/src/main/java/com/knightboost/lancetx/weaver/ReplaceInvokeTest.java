package com.knightboost.lancetx.weaver;

import android.util.Log;

import com.knightboost.lancet.api.Scope;
import com.knightboost.lancet.api.annotations.Group;
import com.knightboost.lancet.api.annotations.ReplaceInvoke;
import com.knightboost.lancet.api.annotations.TargetClass;
import com.knightboost.lancet.api.annotations.TargetMethod;
import com.knightboost.lancet.api.annotations.Weaver;
import com.knightboost.lancetx.ClassA;

@Weaver
@Group("replaceInvokeTest")
public class ReplaceInvokeTest {
    private static final String BASE_ACTIVITY = "android.app.Activity";

    @ReplaceInvoke(isStatic = true)
    @TargetClass(value = "android.util.Log",scope = Scope.SELF)
    @TargetMethod(methodName = "i")
    public static int replaceLogI2(String tag,String msg){
        msg = msg + "被替换";
        return Log.e("zxw",msg);
    }

    @ReplaceInvoke()
    @TargetClass(value = "com.knightboost.lancetx.ClassA",scope = Scope.SELF)
    @TargetMethod(methodName = "printMessage")
    public static void printMessage(ClassA a, String msg){
        msg = msg + "被替换 <- printMessage";
        Log.e("ClassA",msg);
    }
}
