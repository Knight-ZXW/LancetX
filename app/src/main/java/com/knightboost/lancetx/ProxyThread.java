package com.knightboost.lancetx;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ProxyThread extends Thread{

    public ProxyThread(){
        System.out.println("ProxyThread created");
    }

    public ProxyThread(@Nullable Runnable target) {
        super(target);
    }

    public ProxyThread(@NonNull String name) {
        super(name);
    }
}
