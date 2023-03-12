package com.knightboost.lancetx.weaver;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class WrappedIntent extends Intent {

    public WrappedIntent(Intent o) {
        super(o);
    }

    public WrappedIntent(String action) {
        super(action);
        Throwable throwable = new Throwable();
        Log.e("LancetXProxy", "newIntent 调用了", throwable);
    }

    public WrappedIntent(String action, Uri uri) {
        super(action, uri);
    }

    public WrappedIntent(Context packageContext, Class<?> cls) {
        super(packageContext, cls);
    }

    public WrappedIntent(String action, Uri uri, Context packageContext, Class<?> cls) {
        super(action, uri, packageContext, cls);
    }
}
