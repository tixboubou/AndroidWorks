package com.google.android.gms.internal.base;

import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;

public final class zak extends LruCache<Object, Drawable> {
    public zak() {
        super(10);
    }
}
