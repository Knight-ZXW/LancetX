package com.knightboost.lancetx.weaver;

import android.content.Intent;

import com.knightboost.lancet.api.Origin;
import com.knightboost.lancet.api.Scope;

import com.knightboost.lancet.api.annotations.ReplaceNewInvoke;
import com.knightboost.lancet.api.annotations.Weaver;
import com.knightboost.lancetx.ProxyThread;

@Weaver
public class ReplaceNewTest {

    @ReplaceNewInvoke()
    public static void replaceNewThread(Thread t, ProxyThread proxyThread){
    }

    @ReplaceNewInvoke()
    public static void replaceIntent(Intent intent, WrappedIntent newIntent){
    }
}
