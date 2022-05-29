package com.knightboost.lancetx.weaver;

import android.util.Log;

import com.knightboost.weaver.api.Origin;
import com.knightboost.weaver.api.Scope;
import com.knightboost.weaver.api.annotations.ClassOf;
import com.knightboost.weaver.api.annotations.Group;
import com.knightboost.weaver.api.annotations.Insert;
import com.knightboost.weaver.api.annotations.Proxy;
import com.knightboost.weaver.api.annotations.ReplaceInvoke;
import com.knightboost.weaver.api.annotations.TargetClass;
import com.knightboost.weaver.api.annotations.TargetMethod;
import com.knightboost.weaver.api.annotations.Weave;

@Weave
@Group("test")
public class T {
    private static final String BASE_ACTIVITY = "android.app.Activity";

//    @Insert("test")
//    @TargetClass(value = "com.knightboost.lancetx.weaver.TO",scope = Scope.SELF)
//    public void test(@ClassOf("com.knightboost.lancetx.weaver.A") Object a,
//                     Integer b,
//                     @ClassOf("com.knightboost.lancetx.weaver.A") Object c) {
//        Log.e("Zxw","修改过");
//    }

    @Proxy(value = "i")
    @TargetClass(value = "android.util.Log",scope = Scope.SELF)
    @TargetMethod(methodName = "i")
    public static int replaceLogI(String tag,String msg){
        msg = msg + "lancet";
        return (int) Origin.call();
    }

    @ReplaceInvoke()
    @TargetClass(value = "android.util.Log",scope = Scope.SELF)
    @TargetMethod(methodName = "i")
    public static int replaceLogI2(String tag,String msg){
        msg = msg + "lancet";
        return Log.e("zxw",msg);
    }
}
